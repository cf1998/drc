package com.ctrip.framework.drc.replicator.impl.inbound.schema;

import com.ctrip.framework.drc.core.driver.ConnectionObservable;
import com.ctrip.framework.drc.core.driver.binlog.impl.DrcDdlLogEvent;
import com.ctrip.framework.drc.core.driver.binlog.impl.TableMapLogEvent;
import com.ctrip.framework.drc.core.driver.binlog.manager.AbstractSchemaManager;
import com.ctrip.framework.drc.core.driver.binlog.manager.SchemaManager;
import com.ctrip.framework.drc.core.driver.binlog.manager.TableId;
import com.ctrip.framework.drc.core.driver.binlog.manager.TableInfo;
import com.ctrip.framework.drc.core.driver.binlog.manager.task.RetryTask;
import com.ctrip.framework.drc.core.driver.command.netty.endpoint.DefaultEndPoint;
import com.ctrip.framework.drc.core.exception.DrcServerException;
import com.ctrip.framework.drc.core.monitor.datasource.DataSourceManager;
import com.ctrip.framework.drc.core.monitor.entity.BaseEndpointEntity;
import com.ctrip.framework.drc.core.monitor.reporter.DefaultEventMonitorHolder;
import com.ctrip.framework.drc.core.monitor.reporter.DefaultTransactionMonitorHolder;
import com.ctrip.framework.drc.replicator.impl.inbound.schema.ghost.DDLPredication;
import com.ctrip.framework.drc.replicator.impl.inbound.schema.index.IndexExtractor;
import com.ctrip.framework.drc.replicator.impl.inbound.schema.task.DbCreateTask;
import com.ctrip.framework.drc.replicator.impl.inbound.schema.task.DbRestoreTask;
import com.ctrip.framework.drc.replicator.impl.inbound.schema.task.MySQLInstance;
import com.ctrip.framework.drc.replicator.impl.inbound.transaction.TransactionCache;
import com.ctrip.framework.drc.replicator.store.EventStore;
import com.ctrip.framework.drc.replicator.store.manager.file.FileManager;
import com.ctrip.xpipe.api.endpoint.Endpoint;
import com.ctrip.xpipe.api.observer.Observable;
import com.ctrip.xpipe.utils.VisibleForTesting;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.ctrip.framework.drc.core.driver.binlog.manager.SchemaExtractor.extractColumns;
import static com.ctrip.framework.drc.core.driver.binlog.manager.SchemaExtractor.extractValues;
import static com.ctrip.framework.drc.core.driver.binlog.manager.task.SchemaSnapshotTask.SHOW_TABLES_QUERY;
import static com.ctrip.framework.drc.core.driver.util.MySQLConstants.*;
import static com.ctrip.framework.drc.core.monitor.datasource.DataSourceManager.getDefaultPoolProperties;
import static com.ctrip.framework.drc.core.server.config.SystemConfig.CONNECTION_TIMEOUT;
import static com.ctrip.framework.drc.core.server.config.SystemConfig.DDL_LOGGER;
import static com.ctrip.framework.drc.replicator.impl.inbound.schema.MySQLSchemaManager.SchemaStatus.*;
import static ctrip.framework.drc.mysql.EmbeddedDb.*;

/**
 * for in memory mysql
 *
 * @Author limingdong
 * @create 2020/3/2
 */
public class MySQLSchemaManager extends AbstractSchemaManager implements SchemaManager {

    public static final String INDEX_QUERY = "SELECT INDEX_NAME,COLUMN_NAME FROM information_schema.statistics WHERE `table_schema` = \"%s\" AND `table_name` = \"%s\" and NON_UNIQUE=0 ORDER BY SEQ_IN_INDEX;";

    private static final String INFORMATION_SCHEMA_QUERY = "select * from information_schema.COLUMNS where `TABLE_SCHEMA`=\"%s\" and `TABLE_NAME`=\"%s\"";

    private static final int SOCKET_TIMEOUT = 60000;

    private AtomicReference<Map<String, Map<String, String>>> schemaCache = new AtomicReference<>();

    private MySQLInstance embeddedDb;

    private TransactionCache transactionCache;

    protected EventStore eventStore;

    private SchemaStatus schemaStatus = Uninitialized;

    public MySQLSchemaManager(Endpoint endpoint, int applierPort, String clusterName, BaseEndpointEntity baseEndpointEntity) {
        super(endpoint, applierPort, clusterName);
        this.baseEndpointEntity = baseEndpointEntity;
    }

    @Override
    protected void doInitialize() {
        inMemoryEndpoint = new DefaultEndPoint(host, port, user, password);
        PoolProperties poolProperties = getDefaultPoolProperties(inMemoryEndpoint);
        String timeout = String.format("connectTimeout=%s;socketTimeout=%s", CONNECTION_TIMEOUT, SOCKET_TIMEOUT);
        poolProperties.setConnectionProperties(timeout);
        inMemoryDataSource = DataSourceManager.getInstance().getDataSource(inMemoryEndpoint, poolProperties);

        embeddedDb = isUsed(port)
                            ? new RetryTask<>(new DbRestoreTask(port, registryKey)).call()
                            : new RetryTask<>(new DbCreateTask(port, registryKey)).call();
        if (embeddedDb == null) {
            throw new DrcServerException(String.format("[EmbeddedDb] init error for %s", registryKey));
        }
    }

    @Override
    public TableInfo find(String schema, String table) {
        TableId keys = new TableId(schema, table);
        TableInfo tableMeta = tableInfoMap.get(keys);
        if (tableMeta == null) {
            synchronized (this) {
                tableMeta = tableInfoMap.get(keys);
                if (tableMeta == null) {
                    tableMeta = queryTableInfoByIS(inMemoryDataSource, schema, table);

                    if (tableMeta != null) {
                        tableMeta.setDbName(schema);
                        tableMeta.setTableName(table);
                        tableInfoMap.put(keys, tableMeta);
                    }
                }
            }
        }

        return tableMeta;
    }

    @Override
    public Map<String, Map<String, String>> snapshot() {
        Map<String, Map<String, String>> snapshot = doSnapshot(inMemoryEndpoint);
        if (!CollectionUtils.isEmpty(snapshot)) {
            schemaCache.set(snapshot);
        } else {
            DefaultEventMonitorHolder.getInstance().logEvent("Drc.replicator.schema.snapshot", "Blank");
            if (schemaCache.get() != null) {
                return schemaCache.get();
            }
            return snapshot;
        }
        return snapshot;
    }

    /**
     * clear first, then apply
     */
    @Override
    public boolean clone(Endpoint endpoint) {
        Map<String, Map<String, String>> ddlSchemas = doSnapshot(endpoint);
        boolean res = doClone(ddlSchemas);
        DDL_LOGGER.info("[Dump] remote table info finished");
        return res;
    }

    @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    @Override
    public TableInfo queryTableInfoByIS(DataSource dataSource, String schema, String table) {
        String query = String.format(INFORMATION_SCHEMA_QUERY, schema, table);
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    TableInfo tableInfo = extractColumns(resultSet);
                    String indexQuery = String.format(INDEX_QUERY, schema, table);
                    try (ResultSet indexResultSet = statement.executeQuery(indexQuery)) {
                        List<List<String>> indexes = IndexExtractor.extractIndex(indexResultSet);
                        tableInfo.setIdentifiers(indexes);
                    }
                    return tableInfo;
                }
            }
        } catch (SQLException e) {
            DDL_LOGGER.error("queryTableInfoByIS for {}.{} error", schema, table, e);
        }

        return null;
    }

    /**
     * drc_ddl_log_event
     * @param queryString
     */
    @Override
    public void persistDdl(String dbName, String tableName, String queryString) {
        if (StringUtils.isBlank(dbName)) {
            dbName = StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(tableName)) {
            tableName = StringUtils.EMPTY;
        }
        try {
            if (StringUtils.isNotBlank(queryString)) {
                DrcDdlLogEvent ddlLogEvent = new DrcDdlLogEvent(dbName, queryString, 0, 0);
                transactionCache.add(ddlLogEvent);
            }
        } catch (IOException e) {
            DDL_LOGGER.error("[Write] DRC ddl event error", e);
        }
        DDL_LOGGER.info("[Persist] drc ddl event for {}.{}", dbName, tableName);
    }

    /**
     * drc_table_map_log_event
     * @param tableInfo
     */
    @Override
    public void persistColumnInfo(TableInfo tableInfo, boolean writeDirect) {
        List<TableMapLogEvent.Column> columnList = tableInfo.getColumnList();
        if (columnList == null || columnList.isEmpty()) {
            DDL_LOGGER.info("[Skip] persist drc table map log event for {}.{}", tableInfo.getDbName(), tableInfo.getTableName());
            return;
        }
        String tableName = tableInfo.getTableName();
        if (StringUtils.isNotBlank(tableName) && DDLPredication.mayGhostOps(tableName)) {
            DDL_LOGGER.info("[Skip] persist ghost drc table map log event for {}.{}", tableInfo.getDbName(), tableInfo.getTableName());
            return;
        }
        try {
            TableMapLogEvent tableMapLogEvent = new TableMapLogEvent(0, 0, 0, tableInfo.getDbName().toLowerCase(), tableInfo.getTableName().toLowerCase(), columnList, tableInfo.getIdentifiers());
            if (writeDirect) {
                tableMapLogEvent.write(eventStore);
                tableMapLogEvent.release();
            } else {
                transactionCache.add(tableMapLogEvent);
            }
        } catch (IOException e) {
            DDL_LOGGER.error("[Write] DRC TableMapLogEvent error", e);
        }
        DDL_LOGGER.info("[Persist] drc table map log event for {}.{}", tableInfo.getDbName(), tableInfo.getTableName());
    }

    @VisibleForTesting
    @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public Set<TableId> getTableIds(DataSource dataSource) {
        Set<TableId> tableIds = Sets.newHashSet();
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet dResultSet = statement.executeQuery(SHOW_DATABASES_QUERY)) {
                    List<String> databases = extractValues(dResultSet, null);
                    for (String schema : databases) {
                        if (EXCLUDED_DB.contains(schema)) {
                            continue;
                        }

                        try (ResultSet bResultSet = statement.executeQuery(String.format(SHOW_TABLES_QUERY, schema))) {
                            List<String> tables = extractValues(bResultSet, "BASE TABLE");

                            if (tables.isEmpty()) {
                                tableIds.add(new TableId(schema, null));
                                continue;
                            }

                            for (String table : tables) {
                                tableIds.add(new TableId(schema, table));
                            }
                        }

                    }
                }
            }
        } catch (SQLException e) {
            DDL_LOGGER.error("snapshot error", e);
        }

        return tableIds;
    }

    @Override
    protected void doDispose() {
        tableInfoMap.clear();
        inMemoryDataSource.close(true);
        embeddedDb.destroy();
        ddlMonitorExecutorService.shutdown();
    }

    @Override
    public synchronized void update(Object args, Observable observable) {
        if (observable instanceof ConnectionObservable && schemaStatus == Uninitialized) {
            if(initEmbeddedMySQL()) {
                schemaStatus = Initialized;
                DDL_LOGGER.info("[SchemaStatus] set to Initialized for {}", registryKey);
            }
        }

    }

    private boolean initEmbeddedMySQL() {
        if (shouldInitEmbeddedMySQL()) {
            schemaStatus = Initialing;
            DDL_LOGGER.info("[SchemaStatus] set to Initialing for {}", registryKey);
            return DefaultTransactionMonitorHolder.getInstance().logTransactionSwallowException("DRC.replicator.schema.remote.dump", registryKey, () -> MySQLSchemaManager.this.clone(endpoint));
        }
        return true;
    }

    public void setEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void setTransactionCache(TransactionCache transactionCache) {
        this.transactionCache = transactionCache;
    }

    @Override
    protected boolean shouldInitEmbeddedMySQL() {
        FileManager fileManager = eventStore.getFileManager();
        File logDir = fileManager.getDataDir();
        File[] files = logDir.listFiles();
        return files == null || files.length == 0;
    }

    enum SchemaStatus {

        Uninitialized,

        Initialing,

        Initialized,

    }
}

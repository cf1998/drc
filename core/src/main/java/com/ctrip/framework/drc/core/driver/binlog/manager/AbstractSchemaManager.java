package com.ctrip.framework.drc.core.driver.binlog.manager;

import com.ctrip.framework.drc.core.config.DynamicConfig;
import com.ctrip.framework.drc.core.driver.binlog.constant.QueryType;
import com.ctrip.framework.drc.core.driver.binlog.impl.DrcSchemaSnapshotLogEvent;
import com.ctrip.framework.drc.core.driver.binlog.manager.task.*;
import com.ctrip.framework.drc.core.monitor.datasource.DataSourceManager;
import com.ctrip.framework.drc.core.monitor.entity.BaseEndpointEntity;
import com.ctrip.framework.drc.core.server.utils.ThreadUtils;
import com.ctrip.xpipe.api.endpoint.Endpoint;
import com.ctrip.xpipe.lifecycle.AbstractLifecycle;
import com.ctrip.xpipe.tuple.Pair;
import com.google.common.collect.Maps;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.ctrip.framework.drc.core.driver.binlog.manager.TableOperationManager.transformComment;
import static com.ctrip.framework.drc.core.driver.binlog.manager.TableOperationManager.transformCreatePartition;
import static com.ctrip.framework.drc.core.server.config.SystemConfig.DDL_LOGGER;
import static com.ctrip.framework.drc.core.server.utils.ThreadUtils.getThreadName;

/**
 * Created by mingdongli
 * 2019/9/29 下午8:27.
 */
public abstract class AbstractSchemaManager extends AbstractLifecycle implements SchemaManager {

    protected Map<TableId, TableInfo> tableInfoMap = Maps.newConcurrentMap();

    public static final int PORT_STEP = 10000;

    protected int port;

    protected Endpoint endpoint;

    protected String registryKey;

    protected BaseEndpointEntity baseEndpointEntity;

    protected ExecutorService ddlMonitorExecutorService;

    protected DataSource inMemoryDataSource;

    protected Endpoint inMemoryEndpoint;

    public AbstractSchemaManager(Endpoint endpoint, int port, String registryKey) {
        this.port = transformPort(port);
        logger.info("[Schema] port is {}", port);
        this.endpoint = endpoint;
        this.registryKey = registryKey;
        ddlMonitorExecutorService = ThreadUtils.newSingleThreadExecutor(getThreadName(getClass().getSimpleName(), registryKey));
    }

    @Override
    public boolean recovery(DrcSchemaSnapshotLogEvent snapshotLogEvent) {
        if (!shouldInitEmbeddedMySQL() && DynamicConfig.getInstance().getIndependentEmbeddedMySQLSwitch(registryKey)) {
            DDL_LOGGER.info("[Recovery] DrcSchemaSnapshotLogEvent from binlog skip due to not empty or switch on for {}", registryKey);
            return true;
        }

        Map<String, Map<String, String>> future = snapshotLogEvent.getDdls();
        boolean res = doClone(future);
        DDL_LOGGER.info("[Recovery] DrcSchemaSnapshotLogEvent from binlog finished with result {} for {}", future, registryKey);
        return res;
    }

    protected boolean doClone(Map<String, Map<String, String>> ddlSchemas) {
        new RetryTask<>(new SchemeClearTask(inMemoryEndpoint, inMemoryDataSource)).call();
        Boolean res = new RetryTask<>(new SchemeCloneTask(ddlSchemas, inMemoryEndpoint, inMemoryDataSource, registryKey)).call();
        return res == null ? false : res.booleanValue();
    }

    /**
     * key : dbName
     * value: List<create table>
     * @return
     */
    protected Map<String, Map<String, String>> doSnapshot(Endpoint endpoint) {
        DataSource dataSource = DataSourceManager.getInstance().getDataSource(endpoint);
        Map<String, Map<String, String>> snapshot = new RetryTask<>(new SchemaSnapshotTask(endpoint, dataSource)).call();
        if (snapshot == null) {
            snapshot = Maps.newHashMap();
        }
        return snapshot;
    }

    @Override
    public ApplyResult apply(String schema, String table, String ddl, QueryType queryType, String gtid) {
        if (QueryType.ALTER == queryType) {
            if (TableOperationManager.transformAlterPartition(ddl)) {
                return ApplyResult.from(ApplyResult.Status.PARTITION_SKIP, ddl);
            }
        } else if (QueryType.CREATE == queryType) {
            Pair<Boolean, String> transformRes = transformCreatePartition(ddl);
            if (transformRes.getKey()) {
                DDL_LOGGER.info("[Transform] partition from {} to {} in {}", ddl, transformRes.getValue(), getClass().getSimpleName());
                ddl = transformRes.getValue();
            }
        }
        tableInfoMap.clear();
        synchronized (this) {
            SchemeApplyContext schemeApplyContext = new SchemeApplyContext.Builder()
                    .schema(schema)
                    .table(table)
                    .ddl(ddl)
                    .gtid(gtid)
                    .queryType(queryType)
                    .registryKey(registryKey)
                    .build();
            Boolean res = new RetryTask<>(new SchemeApplyTask(
                    schemeApplyContext, inMemoryEndpoint, inMemoryDataSource,
                    ddlMonitorExecutorService, baseEndpointEntity)).call();
            return res == null ? ApplyResult.from(ApplyResult.Status.FAIL, ddl)
                               : ApplyResult.from(ApplyResult.Status.SUCCESS, ddl);
        }
    }

    public static Map<String, Map<String, String>> transformPartition(Map<String, Map<String, String>> schemas) {
        Map<String, Map<String, String>> res = Maps.newHashMap();
        for (Map.Entry<String, Map<String, String>> dbEntry : schemas.entrySet()) {
            Map<String, String> tables = Maps.newHashMap();
            res.put(dbEntry.getKey(), tables);
            for (Map.Entry<String, String> tableEntry : dbEntry.getValue().entrySet()) {
                Pair<Boolean, String> transformRes = transformComment(tableEntry.getValue());
                if (transformRes.getKey()) {
                    DDL_LOGGER.info("[Transform] {}.{} from {} to {}", dbEntry.getKey(), tableEntry.getKey(), tableEntry.getValue(), transformRes.getValue().trim());
                }
                tables.put(tableEntry.getKey(), transformRes.getValue().trim());
            }
        }
        return res;
    }

    public static int transformPort(int port) {
        return port + PORT_STEP;
    }

    protected abstract boolean shouldInitEmbeddedMySQL();
}

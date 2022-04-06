package com.ctrip.framework.drc.core.driver.binlog.manager.task;

import com.ctrip.framework.drc.core.driver.binlog.manager.SchemaExtractor;
import com.ctrip.framework.drc.core.driver.util.MySQLConstants;
import com.ctrip.xpipe.api.endpoint.Endpoint;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * @Author limingdong
 * @create 2021/4/7
 */
public class SchemeClearTask extends AbstractSchemaTask implements NamedCallable<Boolean> {

    public SchemeClearTask(Endpoint inMemoryEndpoint, DataSource inMemoryDataSource) {
        super(inMemoryEndpoint, inMemoryDataSource);
    }

    @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    @Override
    public Boolean call() throws Exception {
        DDL_LOGGER.info("[Schema] clear start");
        try (Connection connection = inMemoryDataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(MySQLConstants.SHOW_DATABASES_QUERY)) {
                    List<String> databases = SchemaExtractor.extractValues(resultSet, null);
                    for (String schema : databases) {
                        if (MySQLConstants.EXCLUDED_DB.contains(schema)) {
                            continue;
                        }
                        if (!addBatch(statement, String.format(MySQLConstants.DROP_DATABASE, schema))) {
                            DDL_LOGGER.info("[Drop] db {} in batch", schema);
                        }
                    }
                    executeBatch(statement);
                    return true;
                }
            }
        }
    }

}

package com.ctrip.framework.drc.replicator.impl.inbound.schema.task;

import com.ctrip.framework.drc.core.config.DynamicConfig;
import com.ctrip.framework.drc.core.driver.binlog.manager.task.NamedCallable;
import com.ctrip.framework.drc.replicator.impl.inbound.schema.MySQLVariablesConfiguration;
import ctrip.framework.drc.mysql.DbKey;
import ctrip.framework.drc.mysql.EmbeddedDb;

import java.util.Map;

import static com.ctrip.framework.drc.core.server.utils.FileUtil.deleteDirectory;
import static ctrip.framework.drc.mysql.EmbeddedDb.mysqlInstanceDir;

/**
 * @Author limingdong
 * @create 2021/4/7
 */
public class DbCreateTask implements NamedCallable<MySQLInstance> {

    private int port;

    private String registryKey;

    public DbCreateTask(int port, String registryKey) {
        this.port = port;
        this.registryKey = registryKey;
    }

    @Override
    public MySQLInstance call() {
        if (!DynamicConfig.getInstance().getIndependentEmbeddedMySQLSwitch(registryKey)) {
            tryDeleteDirectory();
        }
        Map<String, Object> variables =  MySQLVariablesConfiguration.getInstance().getVariables(registryKey);
        return new MySQLInstanceCreator(new EmbeddedDb().mysqlServer(new DbKey(registryKey, port), variables));
    }

    @Override
    public void afterException(Throwable t) {
        String path = mysqlInstanceDir(registryKey, port);
        deleteDirectory(path);
    }

    private void tryDeleteDirectory() {
        String path = mysqlInstanceDir(registryKey, port);
        deleteDirectory(path);
    }
}

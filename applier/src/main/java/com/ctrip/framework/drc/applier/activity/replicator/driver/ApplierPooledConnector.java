package com.ctrip.framework.drc.applier.activity.replicator.driver;

import com.ctrip.framework.drc.core.driver.MySQLConnector;
import com.ctrip.framework.drc.core.monitor.enums.ModuleEnum;
import com.ctrip.framework.drc.fetcher.activity.replicator.driver.FetcherPooledConnector;
import com.ctrip.xpipe.api.endpoint.Endpoint;

/**
 * Created by mingdongli
 * 2019/9/23 下午5:18.
 */
public class ApplierPooledConnector extends FetcherPooledConnector implements MySQLConnector {

    public ApplierPooledConnector(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public String getModuleName() {
        return ModuleEnum.APPLIER.getDescription();
    }
}

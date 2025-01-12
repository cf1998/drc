package com.ctrip.framework.drc.console.service.impl;

import com.ctrip.framework.drc.console.aop.PossibleRemote;
import com.ctrip.framework.drc.console.monitor.delay.config.DbClusterSourceProvider;
import com.ctrip.framework.drc.console.service.MySqlService;
import com.ctrip.framework.drc.console.utils.MySqlUtils;
import com.ctrip.framework.drc.core.http.ApiResult;
import com.ctrip.framework.drc.core.server.common.filter.table.aviator.AviatorRegexFilter;
import com.ctrip.xpipe.api.endpoint.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ClassName MySqlServiceImpl
 * @Author haodongPan
 * @Date 2022/8/17 19:49
 * @Version: $
 */
@Service
public class MySqlServiceImpl implements MySqlService {
    
    @Autowired
    private DbClusterSourceProvider dbClusterSourceProvider;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @PossibleRemote(path="/api/drc/v1/local/createTblStmts/query",excludeArguments = {"endpoint"})
    public Map<String, String> getCreateTableStatements(String mha, String unionFilter, Endpoint endpoint) {
        AviatorRegexFilter aviatorRegexFilter = new AviatorRegexFilter(unionFilter);
        return MySqlUtils.getDefaultCreateTblStmts(endpoint,aviatorRegexFilter);
    }

    @Override
    @PossibleRemote(path="/api/drc/v1/local/sql/integer/query",excludeArguments = {"endpoint"})
    public Integer getAutoIncrement(String mha, String sql, int index, Endpoint endpoint) {
        return MySqlUtils.getSqlResultInteger(endpoint, sql, index);
    }

    @Override
    @PossibleRemote(path="/api/drc/v1/mha/gtid")
    public String getRealExecutedGtid(String mha) {
        logger.info("[[tag=gtidQuery]] try to getRealExecutedGtid from mha{}",mha);
        Endpoint endpoint = dbClusterSourceProvider.getMasterEndpoint(mha);
        if (endpoint == null) {
            logger.warn("[[tag=gtidQuery]] getRealExecutedGtid from mha{},machine not exist",mha);
            return null;
        } else {
            return MySqlUtils.getUnionExecutedGtid(endpoint);
        }

    }
}

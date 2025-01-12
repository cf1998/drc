package com.ctrip.framework.drc.applier.activity.event;

import com.ctrip.framework.drc.applier.activity.replicator.converter.ApplierByteBufConverter;
import com.ctrip.framework.drc.applier.activity.replicator.driver.ApplierPooledConnector;
import com.ctrip.framework.drc.applier.event.ApplierDrcTableMapEvent;
import com.ctrip.framework.drc.fetcher.event.ApplierDrcGtidEvent;
import com.ctrip.framework.drc.fetcher.event.ApplierGtidEvent;
import com.ctrip.framework.drc.fetcher.event.ApplierXidEvent;
import com.ctrip.framework.drc.applier.resource.condition.Progress;
import com.ctrip.framework.drc.core.driver.binlog.LogEvent;
import com.ctrip.framework.drc.core.driver.binlog.LogEventCallBack;
import com.ctrip.framework.drc.core.driver.binlog.impl.DrcHeartbeatLogEvent;
import com.ctrip.framework.drc.core.driver.schema.data.Columns;
import com.ctrip.framework.drc.fetcher.activity.event.DumpEventActivity;
import com.ctrip.framework.drc.fetcher.activity.replicator.FetcherSlaveServer;
import com.ctrip.framework.drc.fetcher.event.FetcherEvent;
import com.ctrip.framework.drc.fetcher.system.InstanceResource;

import static com.ctrip.framework.drc.core.server.config.SystemConfig.HEARTBEAT_LOGGER;

/**
 * @Author limingdong
 * @create 2021/3/4
 */
public class ApplierDumpEventActivity extends DumpEventActivity<FetcherEvent> {

    @InstanceResource
    public Progress progress;

    public boolean skipEvent = false;

    @Override
    protected FetcherSlaveServer getFetcherSlaveServer() {
        return new FetcherSlaveServer(config, new ApplierPooledConnector(config.getEndpoint()), new ApplierByteBufConverter());
    }

    @Override
    protected void doHandleLogEvent(FetcherEvent event) {
        if (event instanceof ApplierDrcGtidEvent) {
            handleApplierDrcGtidEvent(event);
            return;
        }

        if (event instanceof ApplierGtidEvent) {
            handleApplierGtidEvent(event);
            return;
        }

        if (event instanceof ApplierXidEvent) {
            ((ApplierXidEvent) event).updateDumpPosition(context);
            return;
        }

        if (event instanceof ApplierDrcTableMapEvent) {
            ApplierDrcTableMapEvent drcEvent = (ApplierDrcTableMapEvent) event;
            loggerER.info("- DRC - {}: {}", drcEvent.getSchemaNameDotTableName(),
                    Columns.from(drcEvent.getColumns()).getNames());
        }
    }

    @Override
    protected void afterHandleLogEvent(FetcherEvent logEvent) {
        if (shouldSkip(logEvent)) {
            logEvent.release();
            capacity.release();
            progress.tick();
        } else {
            super.afterHandleLogEvent(logEvent);
        }
    }

    protected void handleApplierDrcGtidEvent(FetcherEvent event) {
        ((ApplierDrcGtidEvent) event).involve(context);
    }

    protected void handleApplierGtidEvent(FetcherEvent event) {
        ((ApplierGtidEvent) event).involve(context);
    }

    protected boolean shouldSkip(FetcherEvent logEvent) {
        return false;
    }

    @Override
    protected boolean heartBeat(LogEvent logEvent, LogEventCallBack logEventCallBack) {
        if (logEvent instanceof DrcHeartbeatLogEvent) {
            HEARTBEAT_LOGGER.info("{} - RECEIVED - {}", registryKey, logEvent.getClass().getSimpleName());
            DrcHeartbeatLogEvent heartbeatLogEvent = (DrcHeartbeatLogEvent) logEvent;
            if (heartbeatLogEvent.shouldTouchProgress()) {
                progress.tick();
                HEARTBEAT_LOGGER.info("{} - Tick - {}", registryKey, logEvent.getClass().getSimpleName());
            }
            logEventCallBack.onHeartHeat();
            try {
                logEvent.release();
            } catch (Exception e) {
                logger.error("[Release] heartbeat event error");
            }
            return true;
        }
        return false;
    }


}

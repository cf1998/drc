package com.ctrip.framework.drc.core.driver.binlog.impl;

import com.ctrip.framework.drc.core.driver.IoCache;
import com.ctrip.framework.drc.core.driver.binlog.LogEvent;
import com.ctrip.framework.drc.core.driver.binlog.constant.LogEventType;
import com.ctrip.framework.drc.core.driver.binlog.header.LogEventHeader;
import com.ctrip.xpipe.utils.VisibleForTesting;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.List;

import static com.ctrip.framework.drc.core.server.config.SystemConfig.EVENT_LOGGER;

/**
 * @Author limingdong
 * @create 2020/4/23
 */
public class TransactionEvent extends AbstractLogEvent implements ITransactionEvent {

    private List<LogEvent> logEvents = Lists.newArrayList();

    private boolean isDdl = false;

    public void addLogEvent(LogEvent logEvent) {
        logEvents.add(logEvent);
    }

    @Override
    public void write(IoCache ioCache) {
        ByteBuf[] byteBufs = new ByteBuf[logEvents.size() * 2];
        int index = -1;

        for (LogEvent logEvent : logEvents) {
            LogEventHeader logEventHeader = logEvent.getLogEventHeader();
            ByteBuf headerBuf;

            if (logEventHeader != null) {
                headerBuf = logEventHeader.getHeaderBuf();
                headerBuf.readerIndex(0);
            } else {
                throw new IllegalStateException("haven’t init this event, can’t start write.");
            }

            ByteBuf payload = logEvent.getPayloadBuf();
            payload.readerIndex(0);
            if (payload != null && headerBuf != null) {
                byteBufs[++index] = headerBuf;
                byteBufs[++index] = payload;
            } else {
                throw new IllegalStateException("haven’t init this event, can’t start write.");
            }
        }
        ioCache.write(Lists.newArrayList(PooledByteBufAllocator.DEFAULT.compositeDirectBuffer().addComponents(true, byteBufs)), isDdl);
    }

    @Override
    public void release() {
        logEvents.forEach(logEvent -> {
            LogEventType logEventType = logEvent.getLogEventType();
            try {
                logEvent.release();
            } catch (Exception e) {
                EVENT_LOGGER.error("released logEventType of {} error", logEventType, e);
            }
        });

    }

    @Override
    public void setDdl(boolean ddl) {
        this.isDdl = ddl;
    }

    @Override
    public List<LogEvent> getEvents() {
        return logEvents;
    }

    @Override
    public void setEvents(List<LogEvent> logEvents) {
        this.logEvents = logEvents;
    }

    @Override
    public boolean passFilter() {
        return true;
    }

    @VisibleForTesting
    public boolean isDdl() {
        return isDdl;
    }
}

package com.ctrip.framework.drc.monitor.module;

/**
 * Created by mingdongli
 * 2019/10/15 上午1:35.
 */
public interface MonitorModule extends DrcModule {

    void startMonitorModule();

    void stopMonitorModule();
}

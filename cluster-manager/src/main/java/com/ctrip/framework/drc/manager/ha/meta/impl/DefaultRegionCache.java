package com.ctrip.framework.drc.manager.ha.meta.impl;

import com.ctrip.framework.drc.core.entity.DbCluster;
import com.ctrip.framework.drc.core.entity.Route;
import com.ctrip.framework.drc.core.server.utils.RouteUtils;
import com.ctrip.framework.drc.manager.config.DataCenterService;
import com.ctrip.framework.drc.manager.config.SourceProvider;
import com.ctrip.framework.drc.manager.ha.config.ClusterManagerConfig;
import com.ctrip.framework.drc.manager.ha.meta.RegionCache;
import com.ctrip.xpipe.api.lifecycle.TopElement;
import com.ctrip.xpipe.api.observer.Observer;
import com.ctrip.xpipe.observer.AbstractLifecycleObservable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author limingdong
 * @create 2022/6/22
 */
@Component
public class DefaultRegionCache extends AbstractLifecycleObservable implements RegionCache, TopElement {

    @Autowired(required = false)
    private SourceProvider sourceProvider;

    @Autowired
    private ClusterManagerConfig config;

    @Autowired
    private DataCenterService dataCenterService;

    private String currentRegion;

    private List<DefaultDcCache> dcCaches = Lists.newArrayList();

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
        currentRegion = dataCenterService.getRegion();
        logger.info("[doInitialize][region]{}", currentRegion);
        Set<String> idcLists = dataCenterService.getRegionIdcMapping().get(currentRegion);
        for (String idc : idcLists) {
            DefaultDcCache dcCache = new DefaultDcCache(config, sourceProvider, idc);
            dcCache.initialize();
            dcCaches.add(dcCache);
        }
    }

    @Override
    protected void doStart() throws Exception {
        for (DefaultDcCache dcCache : dcCaches) {
            dcCache.start();
        }
    }

    @Override
    protected void doStop() throws Exception {
        for (DefaultDcCache dcCache : dcCaches) {
            dcCache.stop();
        }
    }

    @Override
    public Set<String> getClusters() {
        return dcCaches.stream().flatMap(dcCache -> dcCache.getClusters().stream()).collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getBackupDcs(String clusterId) {
        Optional<DefaultDcCache> defaultDcCache = dcCaches.stream().filter(dcCache -> dcCache.getCluster(clusterId) != null).findFirst();
        return defaultDcCache.isPresent() ? defaultDcCache.get().getBackupDcs(clusterId) : Maps.newHashMap();
    }

    @Override
    public DbCluster getCluster(String registryKey) {
        Optional<DefaultDcCache> defaultDcCache = dcCaches.stream().filter(dcCache -> dcCache.getCluster(registryKey) != null).findFirst();
        return defaultDcCache.map(dcCache -> dcCache.getCluster(registryKey)).orElse(null);
    }

    @Override
    public Route randomRoute(String clusterId, String dstDc) {
        Optional<DefaultDcCache> defaultDcCache = dcCaches.stream().filter(dcCache -> dcCache.getCluster(clusterId) != null).findFirst();
        List<Route> routesInDiffSrcDc = Lists.newArrayList();
        defaultDcCache.ifPresentOrElse(
                dcCache -> {
                    Integer orgId = dcCache.getCluster(clusterId).getOrgId();
                    dcCaches.forEach(dcCacheInRegion -> {
                        Route route = dcCacheInRegion.randomRoute(clusterId, dstDc, orgId);
                        routesInDiffSrcDc.add(route);
                    });
                },
                () -> {
                    logger.info("no route available for clusterId:{},dstDc:{}", clusterId, dstDc);
                });
        return RouteUtils.random(routesInDiffSrcDc);
    }

    @Override
    public Route randomRoute(String clusterId, String dstDc, Integer orgId) {
        throw new UnsupportedOperationException("unSupport method, need delegate to one certain dcCache");
    }

    @Override
    public void clusterAdded(DbCluster dbCluster) {
        throw new UnsupportedOperationException("can not support cluster add, need input dc");
    }

    @Override
    public void clusterAdded(String dcId, DbCluster dbCluster) {
        Optional<DefaultDcCache> defaultDcCache = dcCaches.stream().filter(dcCache -> dcId.equalsIgnoreCase(dcCache.getCurrentDc())).findFirst();
        defaultDcCache.ifPresentOrElse(dcCache -> dcCache.clusterAdded(dbCluster), () -> {
            logger.error("add cluster:{} error, input dcId:{} doesn't in current region:{}", dbCluster.getId(), dcId, currentRegion);
            throw new IllegalArgumentException(String.format("add cluster:%s error, input dcId:%s doesn't in current region:%s", dbCluster.getId(), dcId, currentRegion));
        });
    }

    @Override
    public void clusterModified(DbCluster dbCluster) {
        Optional<DefaultDcCache> defaultDcCache = dcCaches.stream().filter(dcCache -> dcCache.getCluster(dbCluster.getId()) != null).findFirst();
        defaultDcCache.ifPresentOrElse(dcCache -> dcCache.clusterModified(dbCluster), () -> {
            logger.error("modify cluster:{} error, cluster doesn't in current region:{}", dbCluster.getId(), currentRegion);
            throw new IllegalArgumentException(String.format("modify cluster:%s error, cluster doesn't in current region:%s", dbCluster.getId(), currentRegion));
        });
    }

    @Override
    public void clusterDeleted(String registryKey) {
        Optional<DefaultDcCache> defaultDcCache = dcCaches.stream().filter(dcCache -> dcCache.getCluster(registryKey) != null).findFirst();
        defaultDcCache.ifPresentOrElse(dcCache -> dcCache.clusterDeleted(registryKey), () -> {
            logger.error("delete cluster:{} error, cluster doesn't in current region:{}", registryKey, currentRegion);
            throw new IllegalArgumentException(String.format("delete cluster:%s error, cluster doesn't in current region:%s", registryKey, currentRegion));
        });
    }

    @Override
    public void addObserver(Observer observer) {
        dcCaches.forEach(dcCache -> dcCache.addObserver(observer));
    }
}

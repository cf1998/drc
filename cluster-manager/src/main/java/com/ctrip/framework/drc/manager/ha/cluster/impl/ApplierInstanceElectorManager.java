package com.ctrip.framework.drc.manager.ha.cluster.impl;

import com.ctrip.framework.drc.core.Constants;
import com.ctrip.framework.drc.core.entity.Applier;
import com.ctrip.framework.drc.core.entity.DbCluster;
import com.ctrip.framework.drc.core.server.config.RegistryKey;
import com.ctrip.framework.drc.core.server.container.ZookeeperValue;
import com.ctrip.framework.drc.manager.ha.config.ClusterZkConfig;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ApplierComparator;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ClusterComparator;
import com.ctrip.xpipe.api.lifecycle.TopElement;
import com.ctrip.xpipe.api.observer.Observer;
import com.ctrip.xpipe.codec.JsonCodec;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.locks.LockInternals;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author limingdong
 * @create 2020/5/7
 */
@Component
public class ApplierInstanceElectorManager extends AbstractInstanceElectorManager implements InstanceElectorManager, Observer, TopElement {

    @Override
    protected String getLeaderPath(String clusterId) {
        return ClusterZkConfig.getApplierLeaderLatchPath(clusterId);
    }

    @Override
    protected String getType() {
        return Constants.ENTITY_APPLIER;
    }

    @Override
    protected boolean watchIfNotWatched(String clusterId) {
        return currentMetaManager.watchApplierIfNotWatched(clusterId);
    }

    @Override
    protected void handleClusterAdd(DbCluster dbCluster) {
        String clusterId = dbCluster.getId();
        try {
            List<Applier> appliers = dbCluster.getAppliers();
            doObserveLeader(clusterId, appliers);
        } catch (Exception e) {
            logger.error("[handleClusterAdd]" + clusterId, e);
        }
    }

    @Override
    protected void handleClusterModified(ClusterComparator comparator) {

        String clusterId = comparator.getCurrent().getId();
        ApplierComparator applierComparator = comparator.getApplierComparator();
        Set<Applier> addedApplier = applierComparator.getAdded();
        doObserveLeader(clusterId, addedApplier);
    }

    private void doObserveLeader(String clusterId, Collection<Applier> appliers) {
        for (Applier applier : appliers) {
            String registryKey = clusterId + "." + applier.getTargetMhaName();
            observerClusterLeader(registryKey);
        }
    }

    protected void updateClusterLeader(String leaderLatchPath, List<ChildData> childrenData, String tmpClusterId){

        RegistryKey registryKey = RegistryKey.from(tmpClusterId);
        String clusterId = registryKey.toString();
        logger.info("[Transfer] {} to {}", tmpClusterId, clusterId);
        List<String> childrenPaths = new LinkedList<>();
        childrenData.forEach(childData -> childrenPaths.add(childData.getPath()));

        logger.info("[updateClusterLeader]{}, {}", clusterId, childrenPaths);

        List<String> sortedChildren = LockInternals.getSortedChildren("latch-", sorter, childrenPaths);

        List<Applier> survivalAppliers = new ArrayList<>(childrenData.size());

        for(String path : sortedChildren){
            for(ChildData childData : childrenData){
                if(path.equals(childData.getPath())){
                    String data = new String(childData.getData());
                    ZookeeperValue zookeeperValue = JsonCodec.INSTANCE.decode(data, ZookeeperValue.class);
                    Applier applier = getApplier(clusterId, zookeeperValue.getIp(), zookeeperValue.getPort(), RegistryKey.getTargetMha(tmpClusterId));
                    if (applier != null) {
                        survivalAppliers.add(applier);
                        logger.info("[Survive] applier {} {}:{}", clusterId, zookeeperValue.getIp(), zookeeperValue.getPort());
                    } else {
                        logger.info("[Survive] applier null for {} {}:{}", clusterId, zookeeperValue.getIp(), zookeeperValue.getPort());
                    }
                    break;
                }
            }
        }

        if(survivalAppliers.size() != childrenData.size()){
            throw new IllegalStateException(String.format("[children data not equal with survival appliers]%s, %s", childrenData, survivalAppliers));
        }

        InstanceActiveElectAlgorithm klea = instanceActiveElectAlgorithmManager.get(clusterId);
        Applier activeApplier = (Applier) klea.select(clusterId, survivalAppliers);  //set master
        currentMetaManager.setSurviveAppliers(clusterId, survivalAppliers, activeApplier);
    }

    private Applier getApplier(String clusterId, String ip, int port, String targetMha) {
        DbCluster dbCluster = regionCache.getCluster(clusterId);
        logger.info("[DbCluster] is {}", dbCluster);
        List<Applier> applierList = dbCluster.getAppliers();
        return applierList.stream().filter(applier -> applier.getIp().equalsIgnoreCase(ip) && applier.getPort() == port && applier.getTargetMhaName().equalsIgnoreCase(targetMha)).findFirst().orElse(null);
    }
}

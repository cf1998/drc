package com.ctrip.framework.drc.manager.ha.cluster.impl;

import com.ctrip.framework.drc.core.entity.Applier;
import com.ctrip.framework.drc.core.entity.DbCluster;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ApplierComparator;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ClusterComparator;
import com.ctrip.framework.drc.core.meta.comparator.MetaComparator;
import com.ctrip.framework.drc.core.meta.comparator.MetaComparatorVisitor;
import com.ctrip.framework.drc.manager.ha.meta.comparator.InstanceComparator;
import com.ctrip.xpipe.api.lifecycle.TopElement;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author limingdong
 * @create 2020/5/6
 */
@Component
public class ApplierInstanceManager extends AbstractInstanceManager implements TopElement {

    @Override
    protected void handleClusterModified(ClusterComparator comparator) {

        String clusterId = comparator.getCurrent().getId();
        ApplierComparator applierComparator = comparator.getApplierComparator();
        applierComparator.accept(new ApplierComparatorVisitor(clusterId));
    }

    @Override
    protected void handleClusterDeleted(DbCluster dbCluster) {
        String clusterId = dbCluster.getId();
        for (Applier applier : dbCluster.getAppliers()) {
            removeApplier(clusterId, applier);
        }
    }

    @Override
    protected void handleClusterAdd(DbCluster dbCluster) {
        String clusterId = dbCluster.getId();
        List<Applier> appliers = dbCluster.getAppliers();
        for (Applier applier : appliers) {
            registerApplier(clusterId, applier);
        }
    }

    private void registerApplier(String clusterId, Applier applier) {
        try {
            instanceStateController.registerApplier(clusterId, applier);
        } catch (Exception e) {
            logger.error(String.format("[addApplier]%s,%s", clusterId, applier), e);
        }
    }

    private void removeApplier(String clusterId, Applier applier) {
        try {
            instanceStateController.removeApplier(clusterId, applier, true);
        } catch (Exception e) {
            logger.error(String.format("[removeApplier]%s,%s", clusterId, applier), e);
        }
    }

    protected class ApplierComparatorVisitor implements MetaComparatorVisitor<Applier> {

        private String clusterId;

        public ApplierComparatorVisitor(String clusterId) {
            this.clusterId = clusterId;
        }

        @Override
        public void visitAdded(Applier added) {
            logger.info("[visitAdded][add shard]{}", added);
            registerApplier(clusterId, added);
        }

        @Override
        public void visitModified(@SuppressWarnings("rawtypes") MetaComparator comparator) {
            InstanceComparator instanceComparator = (InstanceComparator) comparator;
            Applier current = (Applier) instanceComparator.getCurrent();
            Applier future = (Applier) instanceComparator.getFuture();
            logger.info("[visitModified][modify shard]{} to {}", current, future);


        }

        @Override
        public void visitRemoved(Applier removed) {
            logger.info("[visitRemoved][remove shard]{}", removed);
            removeApplier(clusterId, removed);
        }
    }
}

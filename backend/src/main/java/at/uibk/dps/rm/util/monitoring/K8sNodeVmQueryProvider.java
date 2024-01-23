package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

@RequiredArgsConstructor
public class K8sNodeVmQueryProvider implements VmQueryProvider {

    private final VmFilter resourceFilter;

    @Override
    public VmQuery getAvailability() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getCost() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getCpu() {
        return new VmSingleQuery("k8s_node_cpu_total").setFilter(Set.of(resourceFilter));
    }

    @Override
    public VmQuery getCpuUtil() {
        VmQuery k8sNodeCpuTotal = getCpu();
        VmSingleQuery k8sNodeCpuUsed = new VmSingleQuery("k8s_node_cpu_used")
            .setFilter(Set.of(resourceFilter));
        return new VmCompoundQuery(k8sNodeCpuUsed, k8sNodeCpuTotal, '/')
            .setMultiplier(100);
    }

    @Override
    public VmQuery getLatency() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getMemory() {
        return new VmSingleQuery("k8s_node_memory_total_bytes").setFilter(Set.of(resourceFilter));
    }

    @Override
    public VmQuery getMemoryUtil() {
        VmQuery k8sNodeMemoryTotal = getMemory();
        VmSingleQuery k8sNodeMemoryUsed = new VmSingleQuery("k8s_node_memory_used_bytes")
            .setFilter(Set.of(resourceFilter));
        return new VmCompoundQuery(k8sNodeMemoryUsed, k8sNodeMemoryTotal, '/')
            .setMultiplier(100);
    }

    @Override
    public VmQuery getStorage() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getStorageUtil() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getUp() {
        throw new NotImplementedException();
    }

    public VmQuery getResourcesByNodeName(Set<String> nodeNames) {
        return new VmSingleQuery("k8s_node_cpu_total")
            .setFilter(Set.of(resourceFilter, new VmFilter("node", "=~", nodeNames)));
    }
}

package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

/**
 * Provides {@link VmQuery}s for k8s cluster resources.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class K8sClusterVmQueryProvider implements VmQueryProvider {

    private final VmFilter resourceFilter;

    private final VmFilter mainResourceFilter;

    @Override
     public VmQuery getAvailability() {
        VmSingleQuery k8sUpRange = new VmSingleQuery("k8s_up")
            .setFilter(Set.of(mainResourceFilter))
            .setTimeRange("30d");
        return new VmFunctionQuery("avg_over_time", k8sUpRange)
            .setMultiplier(100.0);
    }

    @Override
    public VmQuery getCost() {
        throw new NotImplementedException();
    }

    @Override
     public VmQuery getCpu() {
        return new VmSingleQuery("k8s_cpu_total").setFilter(Set.of(resourceFilter));
    }

    @Override
     public VmQuery getCpuUtil() {
        VmQuery k8sCpuTotal = getCpu();
        VmSingleQuery k8sCpuUsed = new VmSingleQuery("k8s_cpu_used")
            .setFilter(Set.of(resourceFilter));
        return new VmCompoundQuery(k8sCpuUsed, k8sCpuTotal, '/')
            .setMultiplier(100);
    }

    @Override
    public VmQuery getLatency() {
        return new VmSingleQuery("k8s_latency_seconds").setFilter(Set.of(mainResourceFilter));
    }

    @Override
     public VmQuery getMemory() {
        return new VmSingleQuery("k8s_memory_total_bytes").setFilter(Set.of(resourceFilter));
    }

    @Override
     public VmQuery getMemoryUtil() {
        VmQuery k8sMemoryTotal = getMemory();
        VmSingleQuery k8sMemoryUsed = new VmSingleQuery("k8s_memory_used_bytes")
            .setFilter(Set.of(resourceFilter));
        return new VmCompoundQuery(k8sMemoryUsed, k8sMemoryTotal, '/')
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
        return new VmSingleQuery("k8s_up").setFilter(Set.of(mainResourceFilter));
    }
}

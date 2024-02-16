package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

/**
 * Provides {@link VmQuery}s for node exporter resources.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class NodeVmQueryProvider implements VmQueryProvider {

    private final VmFilter resourceFilter;

    private final VmFilter noDeploymentFilter;

    private final VmFilter mountPointFilter;

    private final VmFilter fsTypeFilter;

    @Override
    public VmQuery getAvailability() {
        VmSingleQuery nodeUpRange = new VmSingleQuery("up")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter))
            .setTimeRange("30d");
        return new VmFunctionQuery("avg_over_time", nodeUpRange)
            .setMultiplier(100.0);
    }

    @Override
    public VmQuery getCost() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getCpu() {
        VmSingleQuery nodeCpuSecondsTotal = new VmSingleQuery("node_cpu_seconds_total")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter,
                new VmFilter("mode", "=", Set.of("system"))));
        return new VmFunctionQuery("count", nodeCpuSecondsTotal)
            .setGroupBy(Set.of("resource", "instance"));
    }

    @Override
    public VmQuery getCpuUtil() {
        VmSingleQuery nodeCpuSecondsTotal = new VmSingleQuery("node_cpu_seconds_total")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter,
                new VmFilter("mode", "=", Set.of("idle"))))
            .setTimeRange("5s");
        VmFunctionQuery nodeRate = new VmFunctionQuery("rate", nodeCpuSecondsTotal);
        VmFunctionQuery nodeAvg = new VmFunctionQuery("avg", nodeRate)
            .setGroupBy(Set.of("resource", "instance"))
            .setSummand(-1.0);
        return new VmSimpleQuery(nodeAvg)
            .setMultiplier(-100);
    }

    @Override
    public VmQuery getLatency() {
        return new VmSingleQuery("openfaas_latency_seconds").setFilter(Set.of(resourceFilter));
    }

    @Override
    public VmQuery getMemory() {
        return new VmSingleQuery("node_memory_MemTotal_bytes")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter));
    }

    @Override
    public VmQuery getMemoryUtil() {
        VmSingleQuery nodeMemFree = new VmSingleQuery("node_memory_MemFree_bytes")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter));
        VmSingleQuery nodeMemBuffer = new VmSingleQuery("node_memory_Buffers_bytes")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter));
        VmSingleQuery nodeMemCached = new VmSingleQuery("node_memory_Cached_bytes")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter));
        VmQuery nodeMemTotal = getMemory();
        VmCompoundQuery nodeMemFreeBuffer = new VmCompoundQuery(nodeMemFree, nodeMemBuffer, '+');
        VmCompoundQuery nodeMemUsedTotal = new VmCompoundQuery(nodeMemFreeBuffer, nodeMemCached, '+');
        VmCompoundQuery nodeMemUtilAbsolute = new VmCompoundQuery(nodeMemUsedTotal, nodeMemTotal, '/')
            .setSummand(-1);
        return new VmSimpleQuery(nodeMemUtilAbsolute)
            .setMultiplier(-100);
    }

    @Override
    public VmQuery getStorage() {
        return new VmSingleQuery("node_filesystem_size_bytes")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter, mountPointFilter, fsTypeFilter));
    }

    @Override
    public VmQuery getStorageUtil() {
        VmQuery nodeStorageTotalUtil = getStorage();
        VmSingleQuery nodeStorageAvail = new VmSingleQuery("node_filesystem_avail_bytes")
            .setFilter(Set.of(resourceFilter, noDeploymentFilter, mountPointFilter, fsTypeFilter))
            .setMultiplier(-100.0);
        return new VmCompoundQuery(nodeStorageAvail, nodeStorageTotalUtil, '/')
            .setSummand(100);
    }

    @Override
    public VmQuery getUp() {
        return new VmSingleQuery("up").setFilter(Set.of(resourceFilter, noDeploymentFilter));
    }
}

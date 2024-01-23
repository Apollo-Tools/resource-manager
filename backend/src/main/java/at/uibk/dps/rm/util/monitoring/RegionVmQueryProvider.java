package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

@RequiredArgsConstructor
public class RegionVmQueryProvider implements VmQueryProvider {

    private final VmFilter regionFilter;

    private final Set<String> platformIds;

    private final Set<String> instanceTypes;

    @Override
    public VmQuery getAvailability() {
        VmSingleQuery regionUpRange = new VmSingleQuery("region_up")
            .setFilter(Set.of(regionFilter))
            .setTimeRange("30d");
        return new VmFunctionQuery("avg_over_time", regionUpRange)
            .setMultiplier(100.0);
    }

    @Override
    public VmQuery getCost() {
        VmFilter platformFilter = new VmFilter("platform", "=~", platformIds);
        VmFilter instanceTypeFilter = new VmFilter("instance_type", "=~", instanceTypes);

        return new VmSingleQuery("aws_price_usd")
            .setFilter(Set.of(regionFilter, platformFilter, instanceTypeFilter));
    }

    @Override
    public VmQuery getCpu() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getCpuUtil() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getLatency() {
        return new VmSingleQuery("region_latency_seconds").setFilter(Set.of(regionFilter));
    }

    @Override
    public VmQuery getMemory() {
        throw new NotImplementedException();
    }

    @Override
    public VmQuery getMemoryUtil() {
        throw new NotImplementedException();
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
        return new VmSingleQuery("region_up")
            .setFilter(Set.of(regionFilter));
    }
}

package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmQuery;

public interface VmQueryProvider {

    VmQuery getAvailability();

    VmQuery getCpu();

    VmQuery getCpuUtil();

    VmQuery getMemory();

    VmQuery getMemoryUtil();

    VmQuery getUp();
}

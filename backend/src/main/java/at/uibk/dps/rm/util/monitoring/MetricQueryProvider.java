package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmCompoundQuery;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmSingleQuery;

import java.util.List;
import java.util.Map;

public class MetricQueryProvider {

    public List<VmCompoundQuery> getMetricQuery(MonitoringMetricEnum metricEnum, List<String> resourceIds) {
        switch (metricEnum) {
            case CPU_UTIL:
                VmSingleQuery k8sCpuTotal = new VmSingleQuery("k8s_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sCpuAvailable = new VmSingleQuery("k8s_cpu_available")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sCpuUtil = new VmCompoundQuery(k8sCpuTotal, k8sCpuAvailable, "-");
                return List.of(k8sCpuUtil);
        }
        return null;
    }

}

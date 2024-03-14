package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.monitoring.OpenFaasConnectivity;
import at.uibk.dps.rm.entity.monitoring.RegionConnectivity;

/**
 * Utility class to instantiate objects that are linked to the connectivity entities.
 *
 * @author matthi-g
 */
public class TestConnectivityProvider {

    public static OpenFaasConnectivity createOpenFaasConnectivity(long resourceId, Double latency) {
        OpenFaasConnectivity connectivity = new OpenFaasConnectivity();
        connectivity.setResourceId(resourceId);
        connectivity.setLatencySeconds(latency);
        return connectivity;
    }

    public static RegionConnectivity createRegionConnectivity(Region region, boolean isUp, Double latency) {
        RegionConnectivity connectivity = new RegionConnectivity();
        connectivity.setRegion(region);
        connectivity.setIsOnline(isUp);
        connectivity.setLatencySeconds(latency);
        return connectivity;
    }
}

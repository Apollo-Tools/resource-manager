package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO;

import java.math.BigDecimal;

/**
 * Utility class to instantiate database data transfer objects.
 *
 * @author matthi-g
 */
public class TestDBDTOProvider {

    public static FindAllFunctionDeploymentScrapeTargetsDTO createFindFunctionDeploymentScrapeTarget(long resourceId,
            String baseUrl, int metricsPort) {
        return new FindAllFunctionDeploymentScrapeTargetsDTO(1L, 2L, resourceId,
                baseUrl, metricsPort);
    }

    public static FindAllOpenFaaSScrapeTargetsDTO createFindOpenFaaSScrapeTarget(long resourceId,
            String baseUrl, BigDecimal metricsPort) {
        return new FindAllOpenFaaSScrapeTargetsDTO(resourceId, baseUrl, metricsPort);
    }
}

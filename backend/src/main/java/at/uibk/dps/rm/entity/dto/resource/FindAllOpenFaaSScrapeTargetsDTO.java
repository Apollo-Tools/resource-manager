package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents the database result of the ScrapeTargetRepository.findAllOpenFaaSTargets
 * method.
 *
 * @author matthi-g
 */
@Getter
@Setter
@AllArgsConstructor
public class FindAllOpenFaaSScrapeTargetsDTO {

    private long resourceId;

    private String baseUrl;

    private BigDecimal metricsPort;

}

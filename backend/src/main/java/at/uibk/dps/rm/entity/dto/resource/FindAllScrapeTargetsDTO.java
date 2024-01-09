package at.uibk.dps.rm.entity.dto.resource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAllScrapeTargetsDTO {

    private long resourceDeploymentId;

    private long deploymentId;

    private long resourceId;

    private String scrapeUrl;
}

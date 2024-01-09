package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FindAllOpenFaaSScrapeTargetsDTO {

    private long resourceId;

    private String scrapeUrl;

}

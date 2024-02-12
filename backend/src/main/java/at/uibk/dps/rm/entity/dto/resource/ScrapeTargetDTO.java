package at.uibk.dps.rm.entity.dto.resource;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents the response of the listScrapeTargets request.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class ScrapeTargetDTO {

    private List<String> targets;

    private Map<String, String> labels;
}

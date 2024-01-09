package at.uibk.dps.rm.entity.dto.resource;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ScrapeTargetDTO {

    private List<String> targets;

    private Map<String, String> labels;
}

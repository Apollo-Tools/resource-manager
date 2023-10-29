package at.uibk.dps.rm.util.toscamapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeTemplate {
    private String type;
    private List<Map<String, Requirement>> requirements;
    private Map<String, Capability> capabilities;

    public void setType(String type) {
        this.type = type;
    }

    public void setRequirements(List<Map<String, Requirement>> requirements) {
        this.requirements = requirements;
    }

    public void setCapabilities(String key, Capability capabilities) {
        if(this.capabilities == null) {
            Map<String,Capability> map =new HashMap<>();
            map.put(key,capabilities);
            this.capabilities = map;
        } else {
            this.capabilities.put(key,capabilities);
        }



    }
}

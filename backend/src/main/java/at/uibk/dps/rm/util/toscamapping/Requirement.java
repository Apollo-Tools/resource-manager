package at.uibk.dps.rm.util.toscamapping;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Requirement {
    private Map<String, Object> requirements = new HashMap<>();

    public Map<String, Object> getRequirements() {
        return requirements;
    }

    @JsonAnySetter
    public void addRequirement(String key, Object value) {
        requirements.put(key, value);
    }
}

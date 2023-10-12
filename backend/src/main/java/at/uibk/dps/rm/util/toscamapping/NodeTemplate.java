package at.uibk.dps.rm.util.toscamapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeTemplate {
    private String type;
    private List<Map<String, Requirement>> requirements;
    private Map<String, Capability> capabilities;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Map<String, Requirement>> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Map<String, Requirement>> requirements) {
        this.requirements = requirements;
    }

    public Map<String, Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Capability> capabilities) {
        this.capabilities = capabilities;
    }
}

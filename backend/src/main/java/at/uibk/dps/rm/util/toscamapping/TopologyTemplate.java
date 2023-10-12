package at.uibk.dps.rm.util.toscamapping;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopologyTemplate {
    private Map<String, NodeTemplate> node_templates;

    @JsonAnyGetter
    public Map<String, NodeTemplate> getNode_templates() {
        return node_templates;
    }

    @JsonAnySetter
    public void setNode_templates(Map<String, NodeTemplate> node_templates) {
        this.node_templates = node_templates;
    }
}

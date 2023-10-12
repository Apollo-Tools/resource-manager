package at.uibk.dps.rm.util.toscamapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TOSCAFile {
    private String tosca_definitions_version;
    private String description;
    private TopologyTemplate topology_template;

    public String getTosca_definitions_version() {
        return tosca_definitions_version;
    }

    public void setTosca_definitions_version(String tosca_definitions_version) {
        this.tosca_definitions_version = tosca_definitions_version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TopologyTemplate getTopology_template() {
        return topology_template;
    }

    public void setTopology_template(TopologyTemplate topology_template) {
        this.topology_template = topology_template;
    }
}

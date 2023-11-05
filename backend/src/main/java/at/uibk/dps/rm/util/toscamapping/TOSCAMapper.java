package at.uibk.dps.rm.util.toscamapping;

import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TOSCAMapper {

    public static TOSCAFile readTOSCA(String toscaString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(toscaString, TOSCAFile.class);
    }

    public static String writeTOSCA(TOSCAFile toscaFile) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.writeValueAsString(toscaFile);
    }

    public static TOSCAFile mapResourceToTosca(List<Resource> resources) {
        TOSCAFile toscaFile = new TOSCAFile();
        toscaFile.setTosca_definitions_version("tosca_simple_yaml_1_3");
        toscaFile.setDescription("Description of resources");
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        Map<String, NodeTemplate> nodeTemplates = new HashMap<>();
        resources.forEach(resource -> {
            NodeTemplate nodeTemplate = new NodeTemplate();
            nodeTemplate.setType("at.uibk.dps.rm.genericResource");
            Capability resourceCapability = new Capability();
            resourceCapability.addProperty("resource_id", resource.getResourceId());
            resourceCapability.addProperty("name", resource.getName());
            resourceCapability.addProperty("created_at", resource.getCreatedAt());
            resourceCapability.addProperty("updated_at", resource.getUpdatedAt());
            if (resource instanceof MainResource) {
                resourceCapability.addProperty("region_id", ((MainResource) resource).getRegion());
                resourceCapability.addProperty("platform_id", ((MainResource) resource).getPlatform());
            }
            nodeTemplate.setCapabilities("resource", resourceCapability);
            Capability metricCapability = new Capability();
            resource.getMetricValues().forEach(
                    metricValue -> metricCapability.addProperty(metricValue.getMetric().getMetric(),
                    metricValue.getValueBool() != null ? metricValue.getValueBool() :
                            metricValue.getValueNumber() != null ? metricValue.getValueNumber() :
                                    metricValue.getValueString()));
            nodeTemplate.setCapabilities("metrics", metricCapability);
            nodeTemplates.put("resource_"+resource.getResourceId().toString(),nodeTemplate);
        });
        topologyTemplate.setNode_templates(nodeTemplates);
        toscaFile.setTopology_template(topologyTemplate);
        return toscaFile;
    }
}




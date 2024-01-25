package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ListResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class can be used to deserialize the ListResourcesBySLOsRequest.
 *
 * @author matthi-g
 */
public class SLORequestDeserializer extends StdDeserializer<SLORequest> {

    private static final long serialVersionUID = 5187456179594582207L;

    /**
     * The deserialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public SLORequestDeserializer() {
        this(null);
    }

    /**
     * Create an instance from the valueClass.
     *
     * @param valueClass the value class
     */
    public SLORequestDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public SLORequest deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException {
        JsonNode mainNode = jsonParser.getCodec().readTree(jsonParser);
        List<JsonNode> slos = StreamSupport.stream(mainNode.get("slos").spliterator(), false)
                .collect(Collectors.toList());
        SLORequest request;
        // GetOneEnsembleRequest
        if (mainNode.has("ensemble_id")) {
            GetOneEnsemble getOneRequest = new GetOneEnsemble();
            getOneRequest.setEnsembleId(mainNode.get("ensemble_id").asLong());
            getOneRequest.setName(mainNode.get("name").asText());
            List<JsonNode> resources = StreamSupport.stream(mainNode.get("resources").spliterator(),
                    false)
                .collect(Collectors.toList());
            getOneRequest.setResources(deserializeResources(jsonParser, resources));
            request = getOneRequest;
        // CreateEnsembleRequest
        } else if (mainNode.has("name")) {
            CreateEnsembleRequest createRequest = new CreateEnsembleRequest();
            createRequest.setName(mainNode.get("name").asText());
            List<JsonNode> resources = StreamSupport.stream(mainNode.get("resources").spliterator(), false)
                .collect(Collectors.toList());
            createRequest.setResources(deserializeResourceIds(jsonParser, resources));
            request = createRequest;
        // ListResourcesBySLOSRequest
        } else {
            request = new ListResourcesBySLOsRequest();
        }
        for(JsonNode node: slos) {
            ServiceLevelObjective slo;
            try (JsonParser subParser = node.traverse(jsonParser.getCodec())) {
                slo = subParser.readValueAs(ServiceLevelObjective.class);
            }
            mapServiceLevelObjectives(slo, request);
        }
        return request;
    }

    private List<ResourceId> deserializeResourceIds(JsonParser jsonParser, List<JsonNode> nodes) throws IOException {
        List<ResourceId> resourceIds = new ArrayList<>();

        for (JsonNode node : nodes) {
            try (JsonParser subParser = node.traverse(jsonParser.getCodec())) {
                resourceIds.add(subParser.readValueAs(ResourceId.class));
            }
        }
        return resourceIds;
    }

    private List<Resource> deserializeResources(JsonParser jsonParser, List<JsonNode> nodes)
        throws IOException {
        List<Resource> resources = new ArrayList<>();
        for (JsonNode node : nodes) {
            try (JsonParser subParser = node.traverse(jsonParser.getCodec())) {
                resources.add(subParser.readValueAs(Resource.class));
            }
        }
        return resources;
    }

    /**
     * Map service level objectives either to the environment-, platform-, regions-, provider-,
     * resourceType- or slo-list.
     *
     * @param slo the service level objectives
     * @param request the list resources by slos request
     */
    private void mapServiceLevelObjectives(ServiceLevelObjective slo, SLORequest request) {
        if (sloMatchesSLOType(slo, SLOType.ENVIRONMENT)) {
            request.setEnvironments(mapSLONumberValuesToList(slo));
        } else if (sloMatchesSLOType(slo, SLOType.RESOURCE_TYPE)) {
            request.setResourceTypes(mapSLONumberValuesToList(slo));
        } else if (sloMatchesSLOType(slo, SLOType.PLATFORM)) {
            request.setPlatforms(mapSLONumberValuesToList(slo));
        } else if (sloMatchesSLOType(slo, SLOType.REGION)) {
            request.setRegions(mapSLONumberValuesToList(slo));
        }else if (sloMatchesSLOType(slo, SLOType.RESOURCE_PROVIDER)) {
            request.setProviders(mapSLONumberValuesToList(slo));
        } else {
            request.getServiceLevelObjectives().add(slo);
        }
    }

    private boolean nonMetricSLOisInvalid(ServiceLevelObjective slo) {
        return slo.getValue().get(0).getValueNumber() == null || !slo.getExpression().equals(ExpressionType.EQ);
    }

    private boolean sloMatchesSLOType(ServiceLevelObjective slo, SLOType sloType) {
        return slo.getName().equals(sloType.getValue());
    }

    private List<Long> mapSLONumberValuesToList(ServiceLevelObjective slo) {
        if (nonMetricSLOisInvalid(slo)) {
            throw new BadInputException();
        }
        return slo.getValue().stream()
                .map(value -> value.getValueNumber()
                .longValue()).collect(Collectors.toList());
    }
}

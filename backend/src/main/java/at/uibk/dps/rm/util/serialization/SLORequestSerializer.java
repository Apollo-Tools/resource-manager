package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.slo.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class can be used to serialize SLORequest instances.
 *
 * @author matthi-g
 */
public class SLORequestSerializer extends StdSerializer<SLORequest> {
    private static final long serialVersionUID = 470707283974578017L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public SLORequestSerializer() {
        this(null);
    }

    /**
     * Create an instance from the sloRequestClass.
     *
     * @param sloRequestClass the class of sloRequest
     */
    public SLORequestSerializer(Class<SLORequest> sloRequestClass) {
        super(sloRequestClass);
    }

    @Override
    public void serialize(SLORequest sloRequest, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
        gen.writeStartObject();
        if (sloRequest instanceof GetOneEnsemble) {
            GetOneEnsemble getOneEnsemble = (GetOneEnsemble) sloRequest;
            gen.writeNumberField("ensemble_id", getOneEnsemble.getEnsembleId());
            gen.writeStringField("name", getOneEnsemble.getName());
            gen.writeObjectField("resources", getOneEnsemble.getResources());
            gen.writeObjectField("created_at", getOneEnsemble.getCreatedAt());
            gen.writeObjectField("updated_at", getOneEnsemble.getUpdatedAt());
        } else if (sloRequest instanceof CreateEnsembleRequest) {
            CreateEnsembleRequest createEnsembleRequest = (CreateEnsembleRequest) sloRequest;
            gen.writeStringField("name", createEnsembleRequest.getName());
            gen.writeObjectField("resources", createEnsembleRequest.getResources());
        }
        List<ServiceLevelObjective> slos = new ArrayList<>(sloRequest.getServiceLevelObjectives());
        mapNonMetricToSLO(sloRequest.getEnvironments(), SLOType.ENVIRONMENT, slos);
        mapNonMetricToSLO(sloRequest.getResourceTypes(), SLOType.RESOURCE_TYPE, slos);
        mapNonMetricToSLO(sloRequest.getPlatforms(), SLOType.PLATFORM, slos);
        mapNonMetricToSLO(sloRequest.getRegions(), SLOType.REGION, slos);
        mapNonMetricToSLO(sloRequest.getProviders(), SLOType.RESOURCE_PROVIDER, slos);
        gen.writeObjectField("slos", slos);
        gen.writeEndObject();
    }

    /**
     * Map non metrics (values) to slos.
     *
     * @param values the value of a non-metric
     * @param sloType the slo typpe of the non-metric
     * @param slos the service level objectives
     */
    private void mapNonMetricToSLO(List<Long> values, SLOType sloType,
                                   List<ServiceLevelObjective> slos) {
        if (values == null) {
            return;
        }
        List<SLOValue> sloValues = values.stream().map(value -> {
            SLOValue sloValue = new SLOValue();
            sloValue.setValueNumber(value);
            sloValue.setSloValueType(SLOValueType.NUMBER);
            return sloValue;
        }).collect(Collectors.toList());
        if (sloValues.isEmpty()) {
            return;
        }
        slos.add(new ServiceLevelObjective(sloType.getValue(), ExpressionType.EQ, sloValues));
    }
}

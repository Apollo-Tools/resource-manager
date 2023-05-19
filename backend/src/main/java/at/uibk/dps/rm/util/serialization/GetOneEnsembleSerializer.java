package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.slo.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class can be used to serialize GetOneEnsemble instances.
 *
 * @author matthi-g
 */
public class GetOneEnsembleSerializer extends StdSerializer<GetOneEnsemble> {
    private static final long serialVersionUID = 470707283974578017L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public GetOneEnsembleSerializer() {
        this(null);
    }

    /**
     * Create an instance from the getOneEnsembleClass.
     *
     * @param getOneEnsembleClass the class of GetOneEnsemble
     */
    public GetOneEnsembleSerializer(Class<GetOneEnsemble> getOneEnsembleClass) {
        super(getOneEnsembleClass);
    }

    @Override
    public void serialize(GetOneEnsemble getOneEnsemble, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("ensemble_id", getOneEnsemble.getEnsembleId());
        gen.writeStringField("name", getOneEnsemble.getName());
        gen.writeObjectField("resources", getOneEnsemble.getResources());
        gen.writeObjectField("created_at", getOneEnsemble.getCreatedAt());
        gen.writeObjectField("updated_at", getOneEnsemble.getUpdatedAt());
        List<ServiceLevelObjective> slos = getOneEnsemble.getServiceLevelObjectives();
        mapNonMetricToSLO(getOneEnsemble.getRegions(), SLOType.REGION, slos);
        mapNonMetricToSLO(getOneEnsemble.getProviders(), SLOType.RESOURCE_PROVIDER, slos);
        mapNonMetricToSLO(getOneEnsemble.getResourceTypes(), SLOType.RESOURCE_TYPE, slos);
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

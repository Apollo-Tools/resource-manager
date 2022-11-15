package at.uibk.dps.rm.entity.dto.slo;

import at.uibk.dps.rm.util.ServiceLevelObjectiveDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonDeserialize(using = ServiceLevelObjectiveDeserializer.class)
public class ServiceLevelObjective {
    private String name;

    private ExpressionType expression;

    private List<SLOValue> value;
}

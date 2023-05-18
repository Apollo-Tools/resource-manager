package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.util.serialization.SLORequestDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the body of the createEnsemble operation.
 *
 * @author matthi-g
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = SLORequestDeserializer.class)
public class CreateEnsembleRequest extends SLORequest {

    private String name;

    private List<ResourceId> resources = new ArrayList<>();
}

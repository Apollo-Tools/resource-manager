package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.util.SLORequestDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = SLORequestDeserializer.class)
public class CreateEnsembleRequest extends SLORequest {

    private String name;

    private List<ResourceId> resources = new ArrayList<>();
}

package at.uibk.dps.rm.entity.dto.ensemble;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.util.serialization.GetOneEnsembleSerializer;
import at.uibk.dps.rm.util.serialization.SLORequestDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = SLORequestDeserializer.class)
@JsonSerialize(using = GetOneEnsembleSerializer.class)
public class GetOneEnsemble extends SLORequest {

    private long ensembleId;

    private String name;

    private List<ResourceEnsemble> resourceEnsembles;
}

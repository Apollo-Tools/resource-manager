package at.uibk.dps.rm.entity.dto.ensemble;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.util.serialization.GetOneEnsembleSerializer;
import at.uibk.dps.rm.util.serialization.SLORequestDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
import java.util.List;


/**
 * Represents the response body of the getEnsemble operation.
 *
 * @author matthi-g
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = SLORequestDeserializer.class)
@JsonSerialize(using = GetOneEnsembleSerializer.class)
public class GetOneEnsemble extends SLORequest {

    private long ensembleId;

    private String name;

    private List<Resource> resources;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}

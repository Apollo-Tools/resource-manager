package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
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

    /**
     * Get an ensemble instance from the accountId
     *
     * @param accountId the id of the account
     * @return the ensemble
     */
    public Ensemble getEnsemble(long accountId) {
        Account createdBy = new Account();
        createdBy.setAccountId(accountId);
        Ensemble ensemble = new Ensemble();
        ensemble.setIsValid(true);
        ensemble.setName(this.getName());
        ensemble.setCreatedBy(createdBy);
        ensemble.setRegions(this.getRegions());
        ensemble.setProviders(this.getProviders());
        ensemble.setResource_types(this.getResourceTypes());
        ensemble.setPlatforms(this.getPlatforms());
        ensemble.setEnvironments(this.getEnvironments());
        return ensemble;
    }
}

package at.uibk.dps.rm.entity.dto.ensemble;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import lombok.Data;

import java.util.List;

@Data
public class GetOneEnsemble {

    private long ensembleId;

    private String name;

    private List<ServiceLevelObjective> serviceLevelObjectives;

    private List<ResourceEnsemble> resourceEnsembleList;



}

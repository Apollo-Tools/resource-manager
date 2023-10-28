package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.SubResource;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A utility class that provides various methods to process ensemble entities.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class EnsembleUtility {

    private final EnsembleRepositoryProvider repositoryProvider;

    /**
     * Fetch all data related to an ensemble and compose a {@link GetOneEnsemble} DTO.
     *
     * @param sm an active session manager
     * @param ensembleId the id of the ensemble
     * @param accountId the id of the account
     * @return a Single that emits the populated GetOneEnsemble DTO
     */
    public Single<GetOneEnsemble> fetchAndPopulateEnsemble(SessionManager sm, long ensembleId, long accountId) {
        GetOneEnsemble response = new GetOneEnsemble();
        return repositoryProvider.getEnsembleRepository()
            .findByIdAndAccountId(sm, ensembleId, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> {
                response.setEnsembleId(ensemble.getEnsembleId());
                response.setName(ensemble.getName());
                response.setRegions(ensemble.getRegions());
                response.setProviders(ensemble.getProviders());
                response.setResourceTypes(ensemble.getResource_types());
                response.setPlatforms(ensemble.getPlatforms());
                response.setEnvironments(ensemble.getEnvironments());
                response.setCreatedAt(ensemble.getCreatedAt());
                response.setUpdatedAt(ensemble.getUpdatedAt());
                return repositoryProvider.getResourceRepository().findAllByEnsembleId(sm, ensemble.getEnsembleId());
            })
            .flatMapObservable(Observable::fromIterable)
            .map(resource -> {
                resource.setIsLocked(resource.getLockedByDeployment() != null);
                if (resource instanceof SubResource) {
                    return new SubResourceDTO((SubResource) resource);
                }
                return resource;
            })
            .toList()
            .flatMap(mappedResources -> {
                response.setResources(mappedResources);
                return repositoryProvider.getEnsembleSLORepository().findAllByEnsembleId(sm, response.getEnsembleId());
            })
            .map(slos -> {
                response.setServiceLevelObjectives(mapEnsembleSLOsToDTO(slos));
                return response;
            });
    }


    /**
     * Map the ensembleSLOs model entity to the ServiceLevelObjective DTO.
     *
     * @param ensembleSLOs the list of ensembleSLOs
     * @return a List of mapped ServiceLevelObjectives
     */
    private List<ServiceLevelObjective> mapEnsembleSLOsToDTO(List<EnsembleSLO> ensembleSLOs) {
        return ensembleSLOs.stream()
            .map(ensembleSLO -> {
                List<SLOValue> sloValues;
                if (ensembleSLO.getValueNumbers() != null) {
                    sloValues = ensembleSLO.getValueNumbers().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueNumber(value);
                        sloValue.setSloValueType(SLOValueType.NUMBER);
                        return sloValue;
                    }).collect(Collectors.toList());
                } else if (ensembleSLO.getValueStrings() != null) {
                    sloValues = ensembleSLO.getValueStrings().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueString(value);
                        sloValue.setSloValueType(SLOValueType.STRING);
                        return sloValue;
                    }).collect(Collectors.toList());
                } else {
                    sloValues = ensembleSLO.getValueBools().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueBool(value);
                        sloValue.setSloValueType(SLOValueType.BOOLEAN);
                        return sloValue;
                    }).collect(Collectors.toList());
                }
                return new ServiceLevelObjective(ensembleSLO.getName(),
                    ensembleSLO.getExpression(), sloValues);
            }).collect(Collectors.toList());
    }

    /**
     * Create an EnsembleSLO from a service level objective and ensemble.
     *
     * @param slo the service level objective
     * @param ensemble the ensemble
     * @return the created EnsembleSLO
     */
    public static EnsembleSLO createEnsembleSLO(ServiceLevelObjective slo, Ensemble ensemble) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setName(slo.getName());
        ensembleSLO.setExpression(slo.getExpression());
        switch (slo.getValue().get(0).getSloValueType()) {
            case NUMBER:
                List<Double> numberValues = slo.getValue().stream()
                    .map(value -> (Double) value.getValueNumber()).collect(Collectors.toList());
                ensembleSLO.setValueNumbers(numberValues);
                break;
            case STRING:
                List<String> stringValues = slo.getValue().stream()
                    .map(SLOValue::getValueString).collect(Collectors.toList());
                ensembleSLO.setValueStrings(stringValues);
                break;
            case BOOLEAN:
                List<Boolean> boolValues = slo.getValue().stream()
                    .map(SLOValue::getValueBool).collect(Collectors.toList());
                ensembleSLO.setValueBools(boolValues);
                break;
        }
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    /**
     * Get the slo status of all resources
     *
     * @param validResources all valid resources
     * @param ensembleResources the resources to validate
     * @return the List of pairs of resource_ids and their validation status being true for valid
     * and false for invalid
     */
    public List<ResourceEnsembleStatus> getResourceEnsembleStatus(List<Resource> validResources,
            List<Resource> ensembleResources) {
        return ensembleResources.stream()
            .map(Resource::getResourceId)
            .map(resourceId -> {
                boolean isValid = validResources.stream()
                    .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId));
                return new ResourceEnsembleStatus(resourceId, isValid);
            })
            .collect(Collectors.toList());
    }
}

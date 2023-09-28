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

@AllArgsConstructor
public class EnsembleUtility {

    private final EnsembleRepositoryProvider repositoryProvider;

    public Single<GetOneEnsemble> fetchAndPopulateEnsemble(SessionManager sm, long id, long accountId) {
        GetOneEnsemble response = new GetOneEnsemble();
        return repositoryProvider.getEnsembleRepository()
            .findByIdAndAccountId(sm, id, accountId)
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
                response.setServiceLevelObjectives(mapEnsembleSLOstoDTO(slos));
                return response;
            });
    }


    /**
     * Map the ensembleSLOs model entity to the ServiceLevelObjective DTO.
     *
     * @param ensembleSLOs the list of ensembleSLOs
     * @return a List of mapped ServiceLevelObjectives
     */
    private List<ServiceLevelObjective> mapEnsembleSLOstoDTO(List<EnsembleSLO> ensembleSLOs) {
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

package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.util.impl.CompletionStages;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This is the implementation of the #EnsembleService.
 *
 * @author matthi-g
 */
public class EnsembleServiceImpl extends DatabaseServiceProxy<Ensemble> implements EnsembleService {

    private final EnsembleRepositoryProvider repositoryProvider;

    private final SLOUtility sloUtility;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param repositoryProvider the necessary repositories
     */
    public EnsembleServiceImpl(EnsembleRepositoryProvider repositoryProvider, SessionFactory sessionFactory) {
        super(repositoryProvider.getEnsembleRepository(), Ensemble.class, sessionFactory);
        this.repositoryProvider = repositoryProvider;
        this.sloUtility = new SLOUtility(repositoryProvider.getResourceRepository(),
            repositoryProvider.getMetricRepository());
    }

    // TODO: fix return values
    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        CreateEnsembleRequest request = data.mapTo(CreateEnsembleRequest.class);
        CompletionStage<Ensemble> create = withTransaction(session ->
            repositoryProvider.getEnsembleRepository().findByNameAndAccountId(session, request.getName(), accountId)
                .thenApply(existingEnsemble -> {
                    ServiceResultValidator.checkExists(existingEnsemble, Ensemble.class);
                    Ensemble ensemble = request.getEnsemble(accountId);
                    session.persist(ensemble);
                    return ensemble;
                })
                .thenCompose(ensemble -> {
                    List<CompletableFuture<Void>> createResourceEnsembles = request.getResources().stream()
                        .map(resourceId -> repositoryProvider.getResourceRepository()
                            .findById(session, resourceId.getResourceId())
                            .thenAccept(resource -> {
                                ServiceResultValidator.checkFound(resource, Resource.class);
                                ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
                                resourceEnsemble.setEnsemble(ensemble);
                                resourceEnsemble.setResource(resource);
                                session.persist(resourceEnsemble);
                            }).toCompletableFuture())
                        .collect(Collectors.toList());
                    List<CompletableFuture<Void>> createEnsembleSLOs = request.getServiceLevelObjectives().stream()
                        .map(slo -> {
                            EnsembleSLO ensembleSLO = createEnsembleSLO(slo, ensemble);
                            session.persist(ensembleSLO);
                            return CompletionStages.voidFuture().toCompletableFuture();
                        })
                        .collect(Collectors.toList());
                    List<CompletableFuture<Void>> completionStages = new ArrayList<>();
                    completionStages.addAll(createEnsembleSLOs);
                    completionStages.addAll(createResourceEnsembles);
                    return CompletableFuture.allOf(completionStages.toArray(CompletableFuture[]::new))
                        .thenApply(result -> ensemble);
                })
        );
        return sessionToFuture(create).map(res -> {
            JsonObject returnObject = JsonObject.mapFrom(res);
            returnObject.remove("slos");
            returnObject.remove("regions");
            returnObject.remove("providers");
            returnObject.remove("resource_types");
            returnObject.remove("environments");
            returnObject.remove("platforms");
            returnObject.remove("created_by");
            return returnObject;
        });
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Ensemble>> findAll = withSession(session ->
            repositoryProvider.getEnsembleRepository().findAll(session));
        return Future
            .fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Ensemble entity: result) {
                    entity.setCreatedBy(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        CompletionStage<List<Ensemble>> findAll = withSession(session ->
            repositoryProvider.getEnsembleRepository().findAllByAccountId(session, accountId));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Ensemble entity: result) {
                    entity.setCreatedBy(null);
                    entity.setResource_types(null);
                    entity.setRegions(null);
                    entity.setProviders(null);
                    entity.setEnvironments(null);
                    entity.setPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        CompletionStage<GetOneEnsemble> findOne = withSession(session ->
            fetchAndPopulateEnsemble(session, id, accountId)
        );
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    private CompletionStage<GetOneEnsemble> fetchAndPopulateEnsemble(Session session, long id, long accountId) {
        GetOneEnsemble response = new GetOneEnsemble();
        return repositoryProvider.getEnsembleRepository().findByIdAndAccountId(session, id, accountId)
            .thenCompose(ensemble -> {
                ServiceResultValidator.checkFound(ensemble, Ensemble.class);
                response.setEnsembleId(ensemble.getEnsembleId());
                response.setName(ensemble.getName());
                response.setRegions(ensemble.getRegions());
                response.setProviders(ensemble.getProviders());
                response.setResourceTypes(ensemble.getResource_types());
                response.setPlatforms(ensemble.getPlatforms());
                response.setEnvironments(ensemble.getEnvironments());
                response.setCreatedAt(ensemble.getCreatedAt());
                response.setUpdatedAt(ensemble.getUpdatedAt());
                return repositoryProvider.getResourceRepository().findAllByEnsembleId(session, ensemble.getEnsembleId());
            })
            .thenCompose(resources -> {
                List<Resource> mappedResources = resources.stream()
                    .map(resource -> {
                        if (resource instanceof SubResource) {
                            session.refresh(resource);
                            return new SubResourceDTO((SubResource) resource);
                        }
                        return resource;
                    })
                    .collect(Collectors.toList());
                response.setResources(mappedResources);
                return repositoryProvider.getEnsembleSLORepository()
                    .findAllByEnsembleId(session, response.getEnsembleId());
            })
            .thenApply(slos -> {
                response.setServiceLevelObjectives(mapEnsembleSLOstoDTO(slos));
                return response;
            });
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Ensemble> findOne = withSession(session ->
            repositoryProvider.getEnsembleRepository().findById(session, id));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
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

    @Override
    public Future<Boolean> existsOneByNameAndAccountId(String name, long accountId) {
        CompletionStage<Ensemble> findOne = withSession(session ->
            repositoryProvider.getEnsembleRepository().findByNameAndAccountId(session, name, accountId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateEnsembleValidity(long ensembleId, boolean isValid) {
        CompletionStage<Integer> updateValidity = withTransaction(session ->
            repositoryProvider.getEnsembleRepository().updateValidity(session, ensembleId, isValid));
        return Future.fromCompletionStage(updateValidity)
            .mapEmpty();
    }

    private EnsembleSLO createEnsembleSLO(ServiceLevelObjective slo, Ensemble ensemble) {
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

    @Override
    public Future<Void> validateCreateEnsembleRequest(JsonObject data) {
        CreateEnsembleRequest requestDTO = data.mapTo(CreateEnsembleRequest.class);
        List<ResourceId> resourceIds = requestDTO.getResources();
        CompletionStage<Void> validateRequest = withSession(session ->
            sloUtility.findAndFilterResourcesBySLOs(session, requestDTO)
                .thenApply(resources -> resourceIds.stream()
                    .map(ResourceId::getResourceId)
                    .allMatch(resourceId -> resources.stream()
                        .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId))))
                .thenAccept(requestFulfillsSLOs -> {
                    if (!requestFulfillsSLOs) {
                        throw new BadInputException("slo mismatch");
                    }
                })
        );
        return sessionToFuture(validateRequest);
    }

    @Override
    public Future<JsonArray> validateExistingEnsemble(long accountId, long ensembleId) {
        CompletionStage<List<ResourceEnsembleStatus>> validateRequest = withSession(session ->
            validateAndUpdateEnsemble(session, ensembleId, accountId));
        return sessionToFuture(validateRequest)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ResourceEnsembleStatus status: result) {
                    objects.add(JsonObject.mapFrom(status));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Void> validateAllExistingEnsembles() {
        CompletionStage<List<Ensemble>> getAll = withSession(session ->
            repositoryProvider.getEnsembleRepository().findAll(session));
        return sessionToFuture(getAll)
            .flatMap(ensembles -> {
                Future<Void> prevFuture = Future.succeededFuture();
                for (Ensemble ensemble : ensembles) {
                    long accountId = ensemble.getCreatedBy().getAccountId();
                    long ensembleId = ensemble.getEnsembleId();
                    prevFuture = prevFuture
                        .flatMap(res -> validateExistingEnsemble(accountId, ensembleId).mapEmpty());
                }
                return prevFuture;
            });
    }

    private CompletionStage<List<ResourceEnsembleStatus>> validateAndUpdateEnsemble(Session session, long ensembleId,
            long accountId) {
        return repositoryProvider.getEnsembleRepository().findByIdAndAccountId(session, ensembleId, accountId)
            .thenCompose(ensemble -> {
                ServiceResultValidator.checkFound(ensemble, Ensemble.class);
                return fetchAndPopulateEnsemble(session, ensembleId, accountId);
            })
            .thenCompose(getOneEnsemble -> sloUtility.findAndFilterResourcesBySLOs(session, getOneEnsemble)
                .thenApply(validResources -> getResourceEnsembleStatus(validResources, getOneEnsemble.getResources()))
            )
            .thenCompose(statusValues -> {
                List<CompletableFuture<Integer>> updateStatusValues = statusValues.stream()
                    .map(status -> repositoryProvider.getEnsembleRepository()
                        .updateValidity(session, ensembleId, status.getIsValid())
                        .toCompletableFuture())
                    .collect(Collectors.toList());
                return CompletableFuture.allOf(updateStatusValues.toArray(CompletableFuture[]::new))
                    .thenApply(res -> statusValues);
            });
    }

    /**
     * Get the slo status of all resources
     *
     * @param validResources all valid resources
     * @param ensembleResources the resources to validate
     * @return the List of pairs of resource_ids and their validation status being true for valid
     * and false for invalid
     */
    private List<ResourceEnsembleStatus> getResourceEnsembleStatus(List<Resource> validResources,
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

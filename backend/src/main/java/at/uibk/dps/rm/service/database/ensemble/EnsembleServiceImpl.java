package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;
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

    private final EnsembleRepository repository;

    private final ResourceRepository resourceRepository;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param repository the ensemble repository
     */
    public EnsembleServiceImpl(EnsembleRepository repository, ResourceRepository resourceRepository,
            Stage.SessionFactory sessionFactory) {
        super(repository, Ensemble.class, sessionFactory);
        this.repository = repository;
        this.resourceRepository = resourceRepository;
    }

    // TODO: fix return values
    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        CreateEnsembleRequest request = data.mapTo(CreateEnsembleRequest.class);
        CompletionStage<Ensemble> create = withTransaction(session ->
            repository.findByNameAndAccountId(session, request.getName(), accountId)
                .thenApply(existingEnsemble -> {
                    ServiceResultValidator.checkExists(existingEnsemble, Ensemble.class);
                    Ensemble ensemble = request.getEnsemble(accountId);
                    session.persist(ensemble);
                    return ensemble;
                })
                .thenCompose(ensemble -> {
                    List<CompletableFuture<Void>> createResourceEnsembles = request.getResources().stream()
                        .map(resourceId -> resourceRepository.findById(session, resourceId.getResourceId())
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
        CompletionStage<List<Ensemble>> findAll = withSession(repository::findAll);
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
            repository.findAllByAccountId(session, accountId));
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
        CompletionStage<Ensemble> findOne = withSession(session ->
            repository.findByIdAndAccountId(session, id, accountId));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Ensemble> findOne = withSession(session -> repository.findById(session, id));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<Boolean> existsOneByNameAndAccountId(String name, long accountId) {
        CompletionStage<Ensemble> findOne = withSession(session ->
            repository.findByNameAndAccountId(session, name, accountId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateEnsembleValidity(long ensembleId, boolean isValid) {
        CompletionStage<Integer> updateValidity = withTransaction(session ->
            repository.updateValidity(session, ensembleId, isValid));
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
}

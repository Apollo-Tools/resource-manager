package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.dto.service.K8sServiceTypeEnum;
import at.uibk.dps.rm.entity.dto.service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ServiceService.
 *
 * @author matthi-g
 */
public class ServiceServiceImpl extends DatabaseServiceProxy<Service> implements ServiceService {

    private final ServiceRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public ServiceServiceImpl(ServiceRepository repository, SessionFactory sessionFactory) {
        super(repository, Service.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Service> findOne = withSession(session -> repository.findByIdAndFetch(session, id)
            .thenCompose(result -> {
                ServiceResultValidator.checkFound(result, Service.class);
                return session.fetch(result.getEnvVars())
                    .thenCompose(res -> session.fetch(result.getVolumeMounts()))
                    .thenApply(res -> result);
            })
        );

        return sessionToFuture(findOne).map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        CompletionStage<Service> findOne = withSession(session ->
            repository.findByIdAndAccountId(session, id, accountId, true));
        return sessionToFuture(findOne).map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Service>> findAll = withSession(repository::findAllAndFetch);
        return sessionToFuture(findAll)
            .map(ServiceServiceImpl::mapServicesToJsonArray);
    }

    @Override
    public Future<JsonArray> findAllAccessibleServices(long accountId) {
        CompletionStage<List<Service>> findAll = withSession((session) -> repository
            .findAllAccessibleAndFetch(session, accountId));
        return sessionToFuture(findAll)
            .map(ServiceServiceImpl::mapServicesToJsonArray);
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        CompletionStage<List<Service>> findAll = withSession(session -> repository.findAllByAccountId(session, accountId));
        return sessionToFuture(findAll)
            .map(ServiceServiceImpl::mapServicesToJsonArray);
    }

    private static JsonArray mapServicesToJsonArray(List<Service> result) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (Service entity: result) {
            entity.setReplicas(null);
            entity.setPorts(null);
            entity.setCpu(null);
            entity.setMemory(null);
            objects.add(JsonObject.mapFrom(entity));
        }
        return new JsonArray(objects);
    }

    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        Service service = data.mapTo(Service.class);
        CompletionStage<Service> create = withTransaction(session ->
            session.find(K8sServiceType.class, service.getK8sServiceType().getServiceTypeId())
                .thenCompose(k8sServiceType -> {
                    ServiceResultValidator.checkFound(k8sServiceType, K8sServiceType.class);
                    service.setK8sServiceType(k8sServiceType);
                    checkServiceTypePorts(k8sServiceType, service.getPorts().size());
                    return repository.findOneByNameTypeAndCreator(session, service.getName(),
                        service.getServiceType().getArtifactTypeId(), accountId);
                })
                .thenCompose(existingService -> {
                    ServiceResultValidator.checkExists(existingService, Service.class);
                    return session.find(ServiceType.class, service.getServiceType().getArtifactTypeId());
                })
                .thenCompose(serviceType -> {
                    ServiceResultValidator.checkFound(serviceType, ServiceType.class);
                    service.setServiceType(serviceType);
                    return session.find(Account.class, accountId);
                })
                .thenCompose(account -> {
                    ServiceResultValidator.checkFound(account, Account.class);
                    service.setCreatedBy(account);
                    return session.persist(service);
                })
                .thenApply(res -> service)
        );
        return sessionToFuture(create).map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> updateOwned(long id, long accountId, JsonObject fields) {
        UpdateServiceDTO updateService = fields.mapTo(UpdateServiceDTO.class);
        CompletionStage<Service> update = withTransaction(session -> repository
            .findByIdAndAccountId(session, id, accountId, false)
            .thenCompose(service -> {
                ServiceResultValidator.checkFound(service, Service.class);
                long k8sServiceTypeId = updateService.getK8sServiceType() != null ?
                    updateService.getK8sServiceType().getServiceTypeId() :
                    service.getK8sServiceType().getServiceTypeId();
                int portAmount = updateService.getPorts() != null ?
                    updateService.getPorts().size() : service.getPorts().size();
                return session.find(K8sServiceType.class, k8sServiceTypeId)
                    .thenCompose(serviceType -> {
                        ServiceResultValidator.checkFound(serviceType, K8sServiceType.class);
                        checkServiceTypePorts(serviceType, portAmount);
                        service.setReplicas(updateNonNullValue(service.getReplicas(), updateService.getReplicas()));
                        service.setCpu(updateNonNullValue(service.getCpu(), updateService.getCpu()));
                        service.setMemory(updateNonNullValue(service.getMemory(), updateService.getMemory()));
                        service.setK8sServiceType(serviceType);
                        service.setPorts(updateNonNullValue(service.getPorts(), updateService.getPorts()));
                        service.setIsPublic(updateNonNullValue(service.getIsPublic(), updateService.getIsPublic()));
                        return session.fetch(service.getEnvVars())
                            .thenCompose(res -> session.fetch(service.getVolumeMounts()));
                    })
                    .thenApply(res -> {
                        service.setEnvVars(updateNonNullValue(service.getEnvVars(), updateService.getEnvVars()));
                        service.setVolumeMounts(updateNonNullValue(service.getVolumeMounts(), updateService.getVolumeMounts()));
                        return service;
                    });
            })
        );
        return sessionToFuture(update).mapEmpty();
    }

    @Override
    public Future<Void> deleteFromAccount(long accountId, long id) {
        CompletionStage<Void> delete = withTransaction(session ->
            repository.findByIdAndAccountId(session, id, accountId, false)
                .thenCompose(service -> {
                    ServiceResultValidator.checkFound(service, Service.class);
                    return session.remove(service);
                })
        );
        return sessionToFuture(delete);
    }

    private void checkServiceTypePorts(K8sServiceType serviceType, int portAmount) {
        K8sServiceTypeEnum serviceTypeEnum = K8sServiceTypeEnum.fromServiceType(serviceType);
        if (!checkHasNoService(serviceTypeEnum, portAmount) && !checkHasService(serviceTypeEnum, portAmount)) {
            throw new BadInputException("invalid ports for service selection");
        }
    }

    private boolean checkHasNoService(K8sServiceTypeEnum serviceType, int portAmount) {
        return serviceType.equals(K8sServiceTypeEnum.NO_SERVICE) && portAmount == 0;
    }

    private boolean checkHasService(K8sServiceTypeEnum serviceType, int portAmount) {
        return !serviceType.equals(K8sServiceTypeEnum.NO_SERVICE) && portAmount > 0;
    }
}

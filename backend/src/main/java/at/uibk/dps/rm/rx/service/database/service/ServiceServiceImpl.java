package at.uibk.dps.rm.rx.service.database.service;

import at.uibk.dps.rm.entity.dto.service.K8sServiceTypeEnum;
import at.uibk.dps.rm.entity.dto.service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.rx.repository.service.ServiceRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;

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
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Service> findOne = withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Service.class)))
            .flatMap(result -> sessionManager.fetch(result.getEnvVars())
                .flatMap(res -> sessionManager.fetch(result.getVolumeMounts()))
                .flatMapMaybe(res -> Maybe.just(result)))
        );
        handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Service> findOne = withTransactionMaybe(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId, true))
            .switchIfEmpty(Maybe.error(new NotFoundException(Service.class)));
        handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Service>> findAll = withTransactionSingle(repository::findAllAndFetch);
        handleSession(findAll.map(ServiceServiceImpl::mapServicesToJsonArray), resultHandler);
    }

    @Override
    public void findAllAccessibleServices(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Service>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllAccessibleAndFetch(sessionManager, accountId));
        handleSession(findAll.map(ServiceServiceImpl::mapServicesToJsonArray), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Service>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByAccountId(sessionManager, accountId));
        handleSession(findAll.map(ServiceServiceImpl::mapServicesToJsonArray), resultHandler);
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
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        Service service = data.mapTo(Service.class);
        Maybe<Service> create = withTransactionMaybe(sessionManager -> sessionManager
            .find(K8sServiceType.class, service.getK8sServiceType().getServiceTypeId())
            .switchIfEmpty(Maybe.error(new NotFoundException(K8sServiceType.class)))
            .flatMap(k8sServiceType -> {
                service.setK8sServiceType(k8sServiceType);
                checkServiceTypePorts(k8sServiceType, service.getPorts().size());
                return repository.findOneByNameTypeAndCreator(sessionManager, service.getName(),
                    service.getServiceType().getArtifactTypeId(), accountId);
            })
            .flatMap(existingService -> Maybe.<ServiceType>error(new AlreadyExistsException(Service.class)))
            .switchIfEmpty(sessionManager.find(ServiceType.class, service.getServiceType().getArtifactTypeId()))
            .switchIfEmpty(Maybe.error(new NotFoundException(ServiceType.class)))
            .flatMap(serviceType -> {
                service.setServiceType(serviceType);
                return sessionManager.find(Account.class, accountId);
            })
            .switchIfEmpty(Maybe.error(new NotFoundException(Account.class)))
            .flatMapSingle(account -> {
                service.setCreatedBy(account);
                return sessionManager.persist(service);
            })
        );
        handleSession(create.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void updateOwned(long id, long accountId, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        UpdateServiceDTO updateService = fields.mapTo(UpdateServiceDTO.class);
        Completable update = withTransactionCompletable(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId, false)
            .switchIfEmpty(Maybe.error(new NotFoundException(Service.class)))
            .flatMapCompletable(service -> {
                long k8sServiceTypeId = updateService.getK8sServiceType() != null ?
                    updateService.getK8sServiceType().getServiceTypeId() :
                    service.getK8sServiceType().getServiceTypeId();
                int portAmount = updateService.getPorts() != null ?
                    updateService.getPorts().size() : service.getPorts().size();
                return sessionManager.find(K8sServiceType.class, k8sServiceTypeId)
                    .switchIfEmpty(Maybe.error(new NotFoundException(K8sServiceType.class)))
                    .flatMapSingle(serviceType -> {
                        checkServiceTypePorts(serviceType, portAmount);
                        service.setReplicas(updateNonNullValue(service.getReplicas(), updateService.getReplicas()));
                        service.setCpu(updateNonNullValue(service.getCpu(), updateService.getCpu()));
                        service.setMemory(updateNonNullValue(service.getMemory(), updateService.getMemory()));
                        service.setK8sServiceType(serviceType);
                        service.setPorts(updateNonNullValue(service.getPorts(), updateService.getPorts()));
                        service.setIsPublic(updateNonNullValue(service.getIsPublic(), updateService.getIsPublic()));
                        return sessionManager.fetch(service.getEnvVars())
                            .flatMap(res -> sessionManager.fetch(service.getVolumeMounts()));
                    })
                    .flatMapCompletable(res -> {
                        service.setEnvVars(updateNonNullValue(service.getEnvVars(), updateService.getEnvVars()));
                        service.setVolumeMounts(updateNonNullValue(service.getVolumeMounts(), updateService.getVolumeMounts()));
                        return Completable.complete();
                    });
            })
        );
        handleSession(update, resultHandler);
    }

    @Override
    public void deleteFromAccount(long accountId, long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = withTransactionCompletable(sessionManager ->
            repository.findByIdAndAccountId(sessionManager, id, accountId, false)
                .switchIfEmpty(Maybe.error(new NotFoundException(Service.class)))
                .flatMapCompletable(sessionManager::remove)
        );
        handleSession(delete, resultHandler);
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

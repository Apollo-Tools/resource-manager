package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.dto.Service.K8sServiceTypeEnum;
import at.uibk.dps.rm.entity.dto.Service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.repository.service.K8sServiceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ServiceService.
 *
 * @author matthi-g
 */
public class ServiceServiceImpl extends DatabaseServiceProxy<Service> implements ServiceService {

    private final ServiceRepository repository;

    private final K8sServiceTypeRepository serviceTypeRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public ServiceServiceImpl(ServiceRepository repository, K8sServiceTypeRepository serviceTypeRepository,
            Stage.SessionFactory sessionFactory) {
        super(repository, Service.class, sessionFactory);
        this.repository = repository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Service> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return sessionToFuture(findOne).map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Service>> findAll = withSession(repository::findAllAndFetch);
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Service entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        Service service = data.mapTo(Service.class);
        CompletionStage<Service> create = withTransaction(session ->
            serviceTypeRepository.findById(session, service.getK8sServiceType().getServiceTypeId())
                .thenCompose(serviceType -> {
                    ServiceResultValidator.checkFound(serviceType, K8sServiceType.class);
                    service.setK8sServiceType(serviceType);
                    checkServiceTypePorts(serviceType, service.getPorts().size());
                    return repository.findOneByName(session, service.getName());
                })
                .thenApply(existingService -> {
                    ServiceResultValidator.checkExists(existingService, Service.class);
                    session.persist(service);
                    return service;
                })
        );
        return sessionToFuture(create).map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> update(long id, JsonObject fields) {
        UpdateServiceDTO updateService = fields.mapTo(UpdateServiceDTO.class);
        CompletionStage<Service> update = withTransaction(session -> repository.findByIdAndFetch(session, id)
            .thenCompose(service -> {
                ServiceResultValidator.checkFound(service, Service.class);
                long serviceTypeId = (fields.containsKey("service_type") ?
                    fields.getJsonObject("service_type").getLong("service_type_id") :
                    service.getK8sServiceType().getServiceTypeId());
                int portAmount = fields.containsKey("ports") ?
                    fields.getJsonArray("ports").size() : service.getPorts().size();
                return serviceTypeRepository.findById(session, serviceTypeId)
                    .thenApply(serviceType -> {
                        ServiceResultValidator.checkFound(serviceType, K8sServiceType.class);
                        checkServiceTypePorts(serviceType, portAmount);
                        service.setReplicas(updateNonNullValue(service.getReplicas(), updateService.getReplicas()));
                        service.setCpu(updateNonNullValue(service.getCpu(), updateService.getCpu()));
                        service.setMemory(updateNonNullValue(service.getMemory(), updateService.getMemory()));
                        service.setK8sServiceType(serviceType);
                        service.setPorts(updateNonNullValue(service.getPorts(), updateService.getPorts()));
                        return service;
                    });
            })
        );
        return sessionToFuture(update).mapEmpty();
    }

    @Override
    public Future<Boolean> existsAllByIds(Set<Long> serviceIds) {
        CompletionStage<List<Service>> findAll = withSession(session ->
            repository.findAllByIds(session, serviceIds));
        return Future.fromCompletionStage(findAll)
            .map(result -> result.size() == serviceIds.size());
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

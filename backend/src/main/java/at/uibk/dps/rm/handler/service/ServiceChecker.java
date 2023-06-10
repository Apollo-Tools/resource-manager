package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

/**
 * Implements methods to perform CRUD operations on the service entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ServiceChecker extends EntityChecker {

    private final ServiceService service;
    /**
     * Create an instance from the service service.
     *
     * @param service the service to use
     */
    public ServiceChecker(ServiceService service) {
        super(service);
        this.service = service;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsByName = service.existsOneByName(entity.getString("name"));
        return ErrorHandler.handleDuplicates(existsByName).ignoreElement();
    }

    /**
     * Check if all services from the given list exist by their service id.
     *
     * @param serviceResourceIds the list of service resources
     * @return a Completable if all services exist, else an NotFoundException
     * gets thrown
     */
    public Completable checkExistAllByIds(List<ServiceResourceIds> serviceResourceIds) {
        Single<Boolean> existsAllByServiceIds =  Observable.fromIterable(serviceResourceIds)
            .map(ServiceResourceIds::getServiceId)
            .toList()
            .map(Set::copyOf)
            .flatMap(service::existsAllByIds);
        return ErrorHandler.handleExistsOne(existsAllByServiceIds).ignoreElement();

    }
}

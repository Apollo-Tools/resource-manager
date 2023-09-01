package at.uibk.dps.rm.rx.handler.service;

import at.uibk.dps.rm.entity.dto.service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.util.validation.CollectionValidator;
import at.uibk.dps.rm.util.validation.EntityNameValidator;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to validate the inputs of the service endpoint and fails the context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class ServiceInputHandler {
    /**
     * Validate an add service request.
     *
     * @param rc the routing context
     */
    public static void validateAddServiceRequest(RoutingContext rc) {
        Service service = rc.body().asJsonObject().mapTo(Service.class);
        EntityNameValidator.checkName(service.getName(), Service.class)
            .andThen(CollectionValidator.hasDuplicates(service.getPorts()))
            .andThen(CollectionValidator.hasDuplicates(service.getEnvVars()))
            .andThen(CollectionValidator.hasDuplicates(service.getVolumeMounts()))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Validate an update service request
     *
     * @param rc the routing context
     */
    public static void validateUpdateServiceRequest(RoutingContext rc) {
        UpdateServiceDTO updateServiceDTO = rc.body().asJsonObject().mapTo(UpdateServiceDTO.class);
        List<Completable> checks = new ArrayList<>();
        if (updateServiceDTO.getPorts() != null) {
            checks.add(CollectionValidator.hasDuplicates(updateServiceDTO.getPorts()));
        }
        if (updateServiceDTO.getEnvVars() != null) {
            checks.add(CollectionValidator.hasDuplicates(updateServiceDTO.getEnvVars()));
        }
        if (updateServiceDTO.getVolumeMounts() != null) {
            checks.add(CollectionValidator.hasDuplicates(updateServiceDTO.getVolumeMounts()));
        }
        Completable.merge(checks).subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }
}

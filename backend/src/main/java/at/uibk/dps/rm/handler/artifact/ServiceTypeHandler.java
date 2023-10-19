package at.uibk.dps.rm.handler.artifact;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.artifact.ServiceTypeService;

/**
 * Processes the http requests that concern the service artifact type entity.
 *
 * @author matthi-g
 */
public class ServiceTypeHandler extends ValidationHandler {
    /**
     * Create an instance from the serviceTypeChecker.
     *
     * @param serviceTypeService the service
     */
    public ServiceTypeHandler(ServiceTypeService serviceTypeService) {
        super(serviceTypeService);
    }
}

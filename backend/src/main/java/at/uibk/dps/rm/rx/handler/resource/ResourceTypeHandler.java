package at.uibk.dps.rm.rx.handler.resource;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.resource.ResourceTypeService;

/**
 * Processes the http requests that concern the resource_type entity.
 *
 * @author matthi-g
 */
public class ResourceTypeHandler extends ValidationHandler {

    /**
     * Create an instance from the resourceTypeService.
     *
     * @param resourceTypeService the service
     */
    public ResourceTypeHandler(ResourceTypeService resourceTypeService) {
        super(resourceTypeService);
    }
}

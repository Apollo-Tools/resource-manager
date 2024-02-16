package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Processes the http requests that concern the scrape targets for the external monitoring system.
 *
 * @author matthi-g
 */
public class ScrapeTargetHandler extends ValidationHandler {

    private final ResourceService resourceService;

    /**
     * Create an instance from the resourceService.
     *
     * @param resourceService the service
     */
    public ScrapeTargetHandler(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
    }

    /**
     * Find all resources that can be scraped by the external monitoring system.
     *
     * @return a Single that emits all found scrape targets JsonArray
     */
    public Single<JsonArray> getAllScrapeTargets() {
        return resourceService.findAllScrapeTargets();
    }
}

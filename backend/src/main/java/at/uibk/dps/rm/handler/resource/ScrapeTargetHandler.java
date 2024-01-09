package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ScrapeTargetService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class ScrapeTargetHandler extends ValidationHandler {

    private final ScrapeTargetService scrapeTargetService;

    /**
     * Create an instance from the scrapeTargetService.
     *
     * @param scrapeTargetService the service
     */
    public ScrapeTargetHandler(ScrapeTargetService scrapeTargetService) {
        super(scrapeTargetService);
        this.scrapeTargetService = scrapeTargetService;
    }

    public Single<JsonArray> getAllScrapeTargets() {
        return scrapeTargetService.findAllScrapeTargets();
    }
}

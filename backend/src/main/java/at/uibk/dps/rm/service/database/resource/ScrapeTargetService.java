package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.ScrapeTargetRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the scrape targets.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ScrapeTargetService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ScrapeTargetService create(ResourceRepository resourceRepository,
            ScrapeTargetRepository scrapeTargetRepository, SessionManagerProvider smProvider) {
        return new ScrapeTargetServiceImpl(resourceRepository, scrapeTargetRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ScrapeTargetService createProxy(Vertx vertx) {
        return new ScrapeTargetServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress("scrape-target"));
    }

    /**
     * Find all current scrape targets.
     *
     * @param resultHandler receives the found scrape targets as JsonArray
     */
    void findAllScrapeTargets(Handler<AsyncResult<JsonArray>> resultHandler);
}

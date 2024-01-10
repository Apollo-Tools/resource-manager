package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.resource.ScrapeTargetDTO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.ScrapeTargetRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * This is the implementation of the {@link ScrapeTargetService}.
 *
 * @author matthi-g
 */
// TODO: change <class>
public class ScrapeTargetServiceImpl extends DatabaseServiceProxy<Resource> implements ScrapeTargetService {

    private final ScrapeTargetRepository scrapeTargetRepository;

    /**
     * Create an instance from the platformRepository.
     *
     * @param repository the platform repository
     */
    public ScrapeTargetServiceImpl(ResourceRepository repository, ScrapeTargetRepository scrapeTargetRepository,
            SessionManagerProvider smProvider) {
        super(repository, Resource.class, smProvider);
        this.scrapeTargetRepository = scrapeTargetRepository;
    }

    @Override
    public String getServiceProxyAddress() {
        return ServiceProxyAddress.getServiceProxyAddress("scrape-target");
    }

    @Override
    public void findAllScrapeTargets(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<ScrapeTargetDTO>> findAll = smProvider.withTransactionSingle(sm -> {
            Observable<ScrapeTargetDTO> findEc2Resources =
                scrapeTargetRepository.findAllFunctionDeploymentTargets(sm)
                .flatMapObservable(Observable::fromIterable)
                .map(findScrapeTarget -> {
                    ScrapeTargetDTO scrapeTarget = new ScrapeTargetDTO();
                    scrapeTarget.setTargets(List.of(findScrapeTarget.getScrapeUrl()));
                    scrapeTarget.setLabels(Map.of("resource", Long.toString(findScrapeTarget.getResourceId()),
                        "resource_deployment", Long.toString(findScrapeTarget.getResourceDeploymentId()),
                        "deployment", Long.toString(findScrapeTarget.getDeploymentId()))
                    );
                    return scrapeTarget;
                });
            Observable<ScrapeTargetDTO> findOpenFaasResources = scrapeTargetRepository.findAllOpenFaaSTargets(sm)
                .flatMapObservable(Observable::fromIterable)
                .map(functionDeployment -> {
                    ScrapeTargetDTO scrapeTarget = new ScrapeTargetDTO();
                    scrapeTarget.setTargets(List.of(functionDeployment.getBaseUrl() + ':' +
                        functionDeployment.getMetricsPort().intValue() + "/metrics"));
                    scrapeTarget.setLabels(Map.of("resource", Long.toString(functionDeployment.getResourceId())));
                    return scrapeTarget;
                });
            return Observable.merge(findEc2Resources, findOpenFaasResources).toList();
        });
        RxVertxHandler.handleSession(findAll.flatMapObservable(Observable::fromIterable)
            .map(JsonObject::mapFrom)
            .toList()
            .map(JsonArray::new), resultHandler);
    }
}

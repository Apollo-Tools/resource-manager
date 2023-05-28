package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.misc.ServiceLevelObjectiveMapper;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes the http requests that concern the resources linked to the ensemble entity.
 *
 * @author matthi-g
 */
public class ResourceEnsembleHandler extends ValidationHandler {

    private final ResourceEnsembleChecker resourceEnsembleChecker;

    private final EnsembleChecker ensembleChecker;

    private final EnsembleSLOChecker ensembleSLOChecker;

    private final ResourceChecker resourceChecker;


    public ResourceEnsembleHandler(ResourceEnsembleChecker resourceEnsembleChecker, EnsembleChecker ensembleChecker,
        EnsembleSLOChecker ensembleSLOChecker, ResourceChecker resourceChecker) {
        super(resourceEnsembleChecker);
        this.resourceEnsembleChecker = resourceEnsembleChecker;
        this.ensembleChecker = ensembleChecker;
        this.ensembleSLOChecker = ensembleSLOChecker;
        this.resourceChecker = resourceChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        ObjectMapper mapper = DatabindCodec.mapper();
        return HttpHelper.getLongPathParam(rc, "ensembleId")
            .flatMap(ensembleId -> ensembleChecker.checkFindOne(ensembleId, accountId)
                .map(ensemble -> ensemble.mapTo(Ensemble.class)))
            .flatMap(ensemble -> ensembleSLOChecker.checkFindAllByEnsemble(ensemble.getEnsembleId())
                .map(slos -> {
                    List<EnsembleSLO> ensembleSLOS = mapper.readValue(slos.toString(), new TypeReference<>() {});
                    return ensembleSLOS.stream()
                        .map(ServiceLevelObjectiveMapper::mapEnsembleSLO)
                        .collect(Collectors.toList());
                })
                .flatMap(slos -> HttpHelper.getLongPathParam(rc, "resourceId")
                    .flatMap(resourceId -> resourceEnsembleChecker
                            .checkForDuplicateEntity(ensemble.getEnsembleId(),resourceId)
                        .andThen(resourceChecker.checkFindOne(resourceId)))
                    .flatMap(resourceJson -> {
                        Resource resource = resourceJson.mapTo(Resource.class);
                        boolean isValidByMetrics = SLOCompareUtility.resourceFilterBySLOValueType(resource, slos);
                        boolean isValidByNonMetrics = SLOCompareUtility.resourceValidByNonMetricSLOS(resource,
                            ensemble);
                        if (isValidByMetrics && isValidByNonMetrics) {
                            return resourceEnsembleChecker.submitCreate(ensemble.getEnsembleId(),
                                resource.getResourceId())
                                .map(resourceEnsemble -> {
                                    JsonObject result = new JsonObject();
                                    result.put("ensemble_id", resourceEnsemble.getJsonObject("ensemble").getLong(
                                        "ensemble_id"));
                                    result.put("resource_id", resourceEnsemble.getJsonObject("resource").getLong(
                                        "resource_id"));
                                    return result;
                                });
                        } else {
                            return Single.error(new BadInputException(
                                "resource does not fulfill service level objectives"));
                        }
                    }))
            );
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "ensembleId")
            .flatMapCompletable(ensembleId -> ensembleChecker.checkFindOne(ensembleId, accountId)
                .flatMap(ensemble -> HttpHelper.getLongPathParam(rc, "resourceId"))
                .flatMap(resourceId -> resourceEnsembleChecker.checkExistsOne(ensembleId, resourceId)
                    .andThen(Single.defer(() -> Single.just(resourceId))))
                .flatMapCompletable(resourceId -> resourceEnsembleChecker.submitDelete(ensembleId, resourceId))
            );
    }
}

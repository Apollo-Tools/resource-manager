package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ReservationResponse;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationHandler extends ValidationHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentChecker.class);

    private final ReservationChecker reservationChecker;

    private final FunctionResourceChecker functionResourceChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    private final CredentialsChecker credentialsChecker;

    private final ResourceReservationStatusChecker statusChecker;

    private final ResourceTypeMetricChecker resourceTypeMetricChecker;

    private final DeploymentHandler deploymentHandler;

    private final VPCChecker vpcChecker;

    public ReservationHandler(ServiceProxyProvider serviceProxyProvider, DeploymentHandler deploymentHandler) {
        super(new ReservationChecker(serviceProxyProvider.getReservationService()));
        this.reservationChecker = (ReservationChecker) super.entityChecker;
        this.resourceReservationChecker = new ResourceReservationChecker(serviceProxyProvider
            .getResourceReservationService());
        this.functionResourceChecker = new FunctionResourceChecker(serviceProxyProvider.getFunctionResourceService());
        this.deploymentHandler = deploymentHandler;
        this.credentialsChecker = new CredentialsChecker(serviceProxyProvider.getCredentialsService());
        this.statusChecker = new ResourceReservationStatusChecker(serviceProxyProvider
            .getResourceReservationStatusService());
        this.resourceTypeMetricChecker = new ResourceTypeMetricChecker(serviceProxyProvider
            .getResourceTypeMetricService());
        this.vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> reservationChecker.checkFindOne(id, rc.user().principal().getLong("account_id")))
            .flatMap(reservation -> {
                if (!reservation.getBoolean("is_active")) {
                    return Single.just(reservation);
                }
                return resourceReservationChecker
                        .checkFindAllByReservationId(reservation.getLong("reservation_id"))
                        .map(resourceReservations -> {
                            reservation.put("resource_reservations", resourceReservations);
                            return reservation;
                        });
            });
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return reservationChecker.checkFindAll(accountId)
            .flatMap(result -> {
                List<Single<JsonObject>> singles = new ArrayList<>();
                for (Object object : result.getList()) {
                    JsonObject reservation = (JsonObject) object;
                    ((JsonObject) object).remove("is_active");
                    ((JsonObject) object).remove("created_by");
                    ReservationResponse reservationResponse = reservation.mapTo(ReservationResponse.class);
                    singles.add(resourceReservationChecker.checkFindAllByReservationId(reservationResponse.getReservationId())
                        .map(resourceReservationChecker::checkCrucialResourceReservationStatus)
                        .map(status -> {
                            reservationResponse.setStatusValue(status);
                            return JsonObject.mapFrom(reservationResponse);
                        }));
                }
                return Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (JsonObject) object)
                    .collect(Collectors.toList()));
            })
            .map(JsonArray::new);
    }

    // TODO: deploy resources
    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        ReserveResourcesRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(ReserveResourcesRequest.class);
        long accountId = rc.user().principal().getLong("account_id");
        List<VPC> vpcList = new ArrayList<>();
        return checkFindFunctionResources(requestDTO.getFunctionResources()).toList()
            .flatMap(functionResources -> checkCredentialsForResources(accountId, functionResources)
                .andThen(checkMissingRequiredMetrics(functionResources))
                .andThen(checkVPCForResources(accountId, functionResources)
                    .map(vpcs -> {
                        vpcList.addAll(vpcs.stream().map(vpc -> vpc.mapTo(VPC.class)).collect(Collectors.toList()));
                        return vpcList;
                    }).ignoreElement()
                )
                .toSingle(() -> functionResources))
            .flatMap(functionResources -> submitCreateReservation(accountId, functionResources))
            /* TODO: if resource is self managed copy trigger url to resource reservation (or ignore self managed resources)
             maybe remove self managed state (use edge instead of self managed vm) */
            .flatMap(resourceReservations -> resourceReservationChecker
                .submitCreateAll(Json.encodeToBuffer(resourceReservations).toJsonArray())
                .andThen(Single.just(JsonObject.mapFrom(resourceReservations.get(0).getReservation())))
                .map(result -> {
                    Reservation reservation = result.mapTo(Reservation.class);
                    deploymentHandler
                        .deployResources(reservation, accountId, requestDTO.getDockerCredentials(), vpcList)
                        .andThen(Completable.defer(() ->
                            resourceReservationChecker.submitUpdateStatus(reservation.getReservationId(),
                                ReservationStatusValue.DEPLOYED)))
                        .doOnError(throwable -> logger.error(throwable.getMessage()))
                        .onErrorResumeNext(throwable -> resourceReservationChecker
                            .submitUpdateStatus(reservation.getReservationId(), ReservationStatusValue.ERROR))
                        .subscribe();
                    return result;
                })
            );
    }

    private Completable checkMissingRequiredMetrics(List<JsonObject> functionResources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceIds = new HashSet<>();
        for (JsonObject functionResource : functionResources) {
            Resource resource = functionResource.mapTo(FunctionResource.class).getResource();
            if (!resourceIds.contains(resource.getResourceId())) {
                completables.add(resourceTypeMetricChecker
                    .checkMissingRequiredResourceTypeMetrics(resource.getResourceId()));
                resourceIds.add(resource.getResourceId());
            }
        }
        return Completable.merge(completables);
    }

    private Single<List<ResourceReservation>> submitCreateReservation(long accountId, List<JsonObject> functionResources) {
        Reservation reservation = new Reservation();
        reservation.setIsActive(true);
        Account account = new Account();
        account.setAccountId(accountId);
        reservation.setCreatedBy(account);
        return entityChecker.submitCreate(JsonObject.mapFrom(reservation))
            .flatMap(reservationJson -> statusChecker.checkFindOneByStatusValue(ReservationStatusValue.NEW.name())
                .flatMap(status -> createResourceReservationList(reservationJson, functionResources,
                    status.mapTo(ResourceReservationStatus.class))));
    }

    private Completable checkCredentialsForResources(long accountId, List<JsonObject> functionResources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        for (JsonObject jsonObject: functionResources) {
            Region region = jsonObject.mapTo(FunctionResource.class).getResource().getRegion();
            long providerId = region.getResourceProvider().getProviderId();
            if (!resourceProviderIds.contains(providerId) && !region.getName().equals("edge")) {
                completables.add(credentialsChecker.checkExistsOneByProviderId(accountId, providerId));
                resourceProviderIds.add(providerId);
            }
        }
        return Completable.merge(completables);
    }

    private Single<List<JsonObject>> checkVPCForResources(long accountId, List<JsonObject> functionResources) {
        List<Single<JsonObject>> singles = new ArrayList<>();
        HashSet<Long> regionIds = new HashSet<>();
        for (JsonObject jsonObject: functionResources) {
            Region region = jsonObject.mapTo(FunctionResource.class).getResource().getRegion();
            long regionId = region.getRegionId();
            if (!regionIds.contains(regionId) && !region.getName().equals("edge")) {
                singles.add(vpcChecker.checkFindOneByRegionIdAndAccountId(regionId, accountId));
                regionIds.add(regionId);
            }
        }
        if (singles.isEmpty()) {
            return Single.just(new ArrayList<>());
        }
        return Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (JsonObject) object)
            .collect(Collectors.toList()));
    }

    // TODO: terminate resources
    @Override
    protected Completable updateOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> reservationChecker.checkFindOne(id, rc.user().principal().getLong("account_id")))
            .flatMap(reservationJson ->
                resourceReservationChecker.submitUpdateStatus(reservationJson.getLong("reservation_id"),
                    ReservationStatusValue.TERMINATING)
                    .andThen(Single.just(reservationJson)))
            .flatMapCompletable(reservationJson -> {
                Reservation reservation = reservationJson.mapTo(Reservation.class);
                deploymentHandler.terminateResources(reservation, accountId)
                    .andThen(Completable.defer(() ->
                        resourceReservationChecker.submitUpdateStatus(reservation.getReservationId(),
                            ReservationStatusValue.TERMINATED)))
                    .doOnError(throwable -> logger.error(throwable.getMessage()))
                    .onErrorResumeNext(throwable -> resourceReservationChecker
                        .submitUpdateStatus(reservation.getReservationId(), ReservationStatusValue.ERROR))
                    .subscribe();
                return Completable.complete();
            });
    }

    private Observable<JsonObject> checkFindFunctionResources(List<FunctionResourceIds> functionResourceIds) {
        return Observable.fromIterable(functionResourceIds)
            .flatMapSingle(ids -> functionResourceChecker
                .checkFindOneByFunctionAndResource(ids.getFunctionId(), ids.getResourceId())
            );
    }

    private Single<List<ResourceReservation>> createResourceReservationList(JsonObject reservationJson,
                                                                            List<JsonObject> functionResources,
                                                                            ResourceReservationStatus status) {
        List<ResourceReservation> resourceReservations = new ArrayList<>();
        Reservation reservation = reservationJson.mapTo(Reservation.class);
        for (JsonObject functionResourceJson : functionResources) {
            FunctionResource functionResource = new FunctionResource();
            functionResource.setFunctionResourceId(functionResourceJson.getLong("function_resource_id"));
            resourceReservations.add(createNewResourceReservation(reservation, functionResource, status));
        }
        return Single.just(resourceReservations);
    }

    private ResourceReservation createNewResourceReservation(Reservation reservation, FunctionResource functionResource,
                                                             ResourceReservationStatus status) {
        ResourceReservation resourceReservation = new ResourceReservation();
        resourceReservation.setReservation(reservation);
        resourceReservation.setFunctionResource(functionResource);
        resourceReservation.setStatus(status);
        return resourceReservation;
    }
}

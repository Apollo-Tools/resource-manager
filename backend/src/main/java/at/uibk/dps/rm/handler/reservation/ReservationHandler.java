package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.ReservationResponse;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.service.database.reservation.ReservationPreconditionChecker;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationHandler extends ValidationHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentChecker.class);

    private final ReservationChecker reservationChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    private final ResourceReservationStatusChecker statusChecker;

    private final DeploymentHandler deploymentHandler;

    private final ReservationErrorHandler reservationErrorHandler;

    private final ReservationPreconditionChecker preconditionChecker;

    public ReservationHandler(ReservationChecker reservationChecker, ResourceReservationChecker resourceReservationChecker,
                              ResourceReservationStatusChecker statusChecker, DeploymentHandler deploymentHandler,
                              ReservationErrorHandler reservationErrorHandler, ReservationPreconditionChecker preconditionChecker) {
        super(reservationChecker);
        this.reservationChecker = reservationChecker;
        this.resourceReservationChecker = resourceReservationChecker;
        this.statusChecker = statusChecker;
        this.deploymentHandler = deploymentHandler;
        this.reservationErrorHandler = reservationErrorHandler;
        this.preconditionChecker = preconditionChecker;
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
                if (singles.isEmpty()) {
                    return Single.just(new ArrayList<>());
                }

                return Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (JsonObject) object)
                    .collect(Collectors.toList()));
            })
            .map(JsonArray::new);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        ReserveResourcesRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(ReserveResourcesRequest.class);
        long accountId = rc.user().principal().getLong("account_id");
        List<VPC> vpcList = new ArrayList<>();
        return preconditionChecker.checkReservationIsValid(requestDTO, accountId, vpcList)
            .flatMap(functionResources -> reservationChecker.submitCreateReservation(accountId)
                .flatMap(reservationJson ->
                    statusChecker.checkFindOneByStatusValue(ReservationStatusValue.NEW.name())
                        .map(statusNew -> statusNew.mapTo(ResourceReservationStatus.class))
                        .flatMap(statusNew -> createResourceReservationList(reservationJson, functionResources,
                            statusNew))
                )
            )
            // TODO: remove self managed state (use edge instead of self managed vm) */
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
                        .onErrorResumeNext(throwable -> reservationErrorHandler.onDeploymentError(accountId,
                            reservation, throwable))
                        .subscribe();
                    return result;
                })
            );
    }

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
                    .onErrorResumeNext(throwable -> reservationErrorHandler.onTerminationError(reservation, throwable))
                    .subscribe();
                return Completable.complete();
            });
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

package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ReservationResponse;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Processes the http requests that concern the reservation entity.
 *
 * @author matthi-g
 */
public class ReservationHandler extends ValidationHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentChecker.class);

    private final ReservationChecker reservationChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    private final ResourceReservationStatusChecker statusChecker;

    private final FunctionReservationChecker functionReservationChecker;

    private final ServiceReservationChecker serviceReservationChecker;


    //TODO: move this to the router
    private final DeploymentHandler deploymentHandler;


    //TODO: move this to the router
    private final ReservationErrorHandler reservationErrorHandler;


    //TODO: move this to the router
    private final ReservationPreconditionHandler preconditionHandler;

    /**
     * Create an instance from the reservationChecker, resourceReservationChecker, statusChecker,
     * deploymentHandler, reservationErrorHandler and preconditionHandler
     *
     * @param reservationChecker the reservation checker
     * @param resourceReservationChecker the resource reservation checker
     * @param statusChecker the status checker
     * @param deploymentHandler the deployment handler
     * @param reservationErrorHandler the reservation error handler
     * @param preconditionHandler the precondition handler
     */
    public ReservationHandler(ReservationChecker reservationChecker, ResourceReservationChecker resourceReservationChecker,
          FunctionReservationChecker functionReservationChecker, ServiceReservationChecker serviceReservationChecker,
          ResourceReservationStatusChecker statusChecker, DeploymentHandler deploymentHandler,
          ReservationErrorHandler reservationErrorHandler, ReservationPreconditionHandler preconditionHandler) {
        super(reservationChecker);
        this.reservationChecker = reservationChecker;
        this.resourceReservationChecker = resourceReservationChecker;
        this.functionReservationChecker = functionReservationChecker;
        this.serviceReservationChecker = serviceReservationChecker;
        this.statusChecker = statusChecker;
        this.deploymentHandler = deploymentHandler;
        this.reservationErrorHandler = reservationErrorHandler;
        this.preconditionHandler = preconditionHandler;
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> reservationChecker.checkFindOne(id, rc.user().principal().getLong("account_id")))
            .flatMap(reservation -> resourceReservationChecker
                    .checkFindAllByReservationId(reservation.getLong("reservation_id"))
                    .map(resourceReservations -> {
                        reservation.put("resource_reservations", resourceReservations);
                        return reservation;
                    }));
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
        return preconditionHandler.checkReservationIsValid(requestDTO, accountId, vpcList)
            .flatMap(resources -> reservationChecker.submitCreateReservation(accountId)
                .flatMap(reservationJson ->
                    statusChecker.checkFindOneByStatusValue(ReservationStatusValue.NEW.name())
                        .map(statusNew -> statusNew.mapTo(ResourceReservationStatus.class))
                        .flatMap(statusNew -> createResourceReservationMap(reservationJson, requestDTO, statusNew))
                    //TODO: remove self managed state (use edge instead of self managed vm) */
                    .flatMap(resourceReservations -> functionReservationChecker
                        .submitCreateAll(Json.encodeToBuffer(resourceReservations.get("function")).toJsonArray())
                        .andThen(serviceReservationChecker.submitCreateAll(Json
                            .encodeToBuffer(resourceReservations.get("service")).toJsonArray()))
                        .andThen(Single.defer(() -> Single.just(1)))
                        .map(res -> {
                            Reservation reservation = reservationJson.mapTo(Reservation.class);
                            initiateDeployment(reservation, accountId, requestDTO, vpcList);
                            return reservationJson;
                        })
                    )
                )
            );
    }

    //TODO: check if already terminated
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
                initiateTermination(reservation, accountId);
                return Completable.complete();
            });
    }
    //TODO: add check for authentication in delete

    private Single<Map<String, List<ResourceReservation>>> createResourceReservationMap(JsonObject reservationJson,
                                                                                  ReserveResourcesRequest request,
                                                                                  ResourceReservationStatus status) {
        List<ResourceReservation> functionReservations = new ArrayList<>();
        List<ResourceReservation> serviceReservations = new ArrayList<>();
        Reservation reservation = reservationJson.mapTo(Reservation.class);
        for (FunctionResourceIds functionResourceIds : request.getFunctionResources()) {
            Resource resource = new Resource();
            resource.setResourceId(functionResourceIds.getResourceId());
            Function function = new Function();
            function.setFunctionId(functionResourceIds.getFunctionId());
            functionReservations.add(createNewResourceReservation(reservation, resource, function, status));
        }
        for (ServiceResourceIds serviceResourceIds : request.getServiceResources()) {
            Resource resource = new Resource();
            resource.setResourceId(serviceResourceIds.getResourceId());
            Service service = new Service();
            service.setServiceId(serviceResourceIds.getServiceId());
            serviceReservations.add(createNewResourceReservation(reservation, resource, service,
                serviceResourceIds.getNamespace(), serviceResourceIds.getContext(), status));
        }
        Map<String, List<ResourceReservation>> resourceReservations = Map.of("function", functionReservations,
            "service", serviceReservations);
        return Single.just(resourceReservations);
    }

    private ResourceReservation createNewResourceReservation(Reservation reservation, Resource resource,
            Function function, ResourceReservationStatus status) {
        FunctionReservation resourceReservation = new FunctionReservation();
        resourceReservation.setReservation(reservation);
        resourceReservation.setResource(resource);
        resourceReservation.setFunction(function);
        resourceReservation.setStatus(status);
        return resourceReservation;
    }

    private ResourceReservation createNewResourceReservation(Reservation reservation, Resource resource,
            Service service, String namespace, String context, ResourceReservationStatus status) {
        ServiceReservation resourceReservation = new ServiceReservation();
        resourceReservation.setReservation(reservation);
        resourceReservation.setResource(resource);
        resourceReservation.setService(service);
        resourceReservation.setStatus(status);
        resourceReservation.setNamespace(namespace);
        resourceReservation.setContext(context);
        return resourceReservation;
    }

    /**
     * Execute the deployment of the resource contained in the reservation.
     *
     * @param reservation the reservation
     * @param accountId the id of the creator of the reservation
     * @param requestDTO the request body
     * @param vpcList the list of vpcs
     */
    // TODO: add check for kubeconfig
    private void initiateDeployment(Reservation reservation, long accountId, ReserveResourcesRequest requestDTO,
                                    List<VPC> vpcList) {
        deploymentHandler
            .deployResources(reservation, accountId, requestDTO.getDockerCredentials(), requestDTO.getKubeConfig(),
                vpcList)
            .andThen(Completable.defer(() ->
                resourceReservationChecker.submitUpdateStatus(reservation.getReservationId(),
                    ReservationStatusValue.DEPLOYED)))
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> reservationErrorHandler.onDeploymentError(accountId,
                reservation, throwable))
            .subscribe();
    }

    /**
     * Execute the termination of the resource contained in the reservation.
     *
     * @param reservation the reservation
     * @param accountId the id of the creator of the reservation
     */
    private void initiateTermination(Reservation reservation, long accountId) {
        deploymentHandler.terminateResources(reservation, accountId)
            .andThen(Completable.defer(() ->
                resourceReservationChecker.submitUpdateStatus(reservation.getReservationId(),
                    ReservationStatusValue.TERMINATED)))
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> reservationErrorHandler.onTerminationError(reservation, throwable))
            .subscribe();
    }
}

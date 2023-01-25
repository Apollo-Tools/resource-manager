package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class ReservationHandler extends ValidationHandler {

    private final ResourceChecker resourceChecker;

    private final ReservationChecker reservationChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    private final MetricValueChecker metricValueChecker;

    private final DeploymentHandler deploymentHandler;

    public ReservationHandler(ServiceProxyProvider serviceProxyProvider, DeploymentHandler deploymentHandler) {
        super(new ReservationChecker(serviceProxyProvider.getReservationService()));
        this.reservationChecker = (ReservationChecker) super.entityChecker;
        this.resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        this.resourceReservationChecker = new ResourceReservationChecker(serviceProxyProvider.getResourceReservationService());
        this.metricValueChecker = new MetricValueChecker(serviceProxyProvider.getMetricValueService());
        this.deploymentHandler = deploymentHandler;
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
                        .flatMap(this::checkFindResourcesFromReservation)
                        .map(resourceReservations -> {
                            reservation.put("resource_reservations", resourceReservations);
                            return reservation;
                        });
            });
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return reservationChecker.checkFindAll(accountId);
    }

    // TODO: deploy resources
    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        ReserveResourcesRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(ReserveResourcesRequest.class);
        long accountId = rc.user().principal().getLong("account_id");
        return Completable
                .merge(checkResourcesExistAndAreNotReserved(requestDTO.getResources()))
                .andThen(Single.just(new Reservation()))
                .flatMap(reservation -> {
                    reservation.setIsActive(true);
                    Account account = new Account();
                    account.setAccountId(accountId);
                    reservation.setCreatedBy(account);
                    return entityChecker.submitCreate(JsonObject.mapFrom(reservation));
                })
                .map(reservationJson -> createResourceReservationList(reservationJson, requestDTO.getResources()))
                .flatMap(resourceReservations -> resourceReservationChecker
                    .submitCreateAll(Json.encodeToBuffer(resourceReservations).toJsonArray())
                    .andThen(Single.just(JsonObject.mapFrom(resourceReservations.get(0).getReservation())))
                    .map(result -> {
                        deploymentHandler
                            .deployResources(result.getLong("reservation_id"), accountId)
                            .subscribe();
                        return result;
                    })
                );
    }

    // TODO: terminate resources
    @Override
    protected Completable updateOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
                .flatMap(id -> reservationChecker.checkFindOne(id, rc.user().principal().getLong("account_id")))
                .flatMapCompletable(reservationChecker::submitCancelReservation);
    }

    private Single<List<JsonObject>> checkFindResourcesFromReservation(JsonArray resourceReservations) {
        return Observable.fromStream(resourceReservations.stream())
                .flatMapSingle(resourceReservationObject -> {
                    ResourceReservation resourceReservation = ((JsonObject) resourceReservationObject)
                            .mapTo(ResourceReservation.class);
                    // TODO FIX
                    Resource resource = null; //resourceReservation.getResource();
                    JsonObject resourceJson = JsonObject.mapFrom(resource);
                    return metricValueChecker.checkFindAllByResource(resource.getResourceId(), true)
                            .map(metricValues -> mapMetricValuesToResourceReservation(
                                    (JsonObject) resourceReservationObject, resourceJson, metricValues)
                            );
                })
                .toList();
    }

    private List<Completable> checkResourcesExistAndAreNotReserved(List<Long> resourceIds) {
        List<Completable> completables = new ArrayList<>();
        resourceIds.forEach(resourceId ->
                completables.add(resourceChecker.checkExistsOneAndIsNotReserved(resourceId)));
        return completables;
    }

    private JsonObject mapMetricValuesToResourceReservation(JsonObject resourceReservationObject, JsonObject resource,
                                                            JsonArray metricValues) {
        resource.put("metric_values", metricValues);
        resourceReservationObject.put("resource", resource);
        return resourceReservationObject;
    }

    private List<ResourceReservation> createResourceReservationList(JsonObject reservationJson, List<Long> resourceIds) {
        List<ResourceReservation> resourceReservations = new ArrayList<>();
        Reservation reservation = reservationJson.mapTo(Reservation.class);
        for (Long resourceId : resourceIds) {
            Resource resource = new Resource();
            resource.setResourceId(resourceId);
            resourceReservations.add(createNewResourceReservation(reservation, resource));
        }
        return resourceReservations;
    }

    private ResourceReservation createNewResourceReservation(Reservation reservation, Resource resource) {
        ResourceReservation resourceReservation = new ResourceReservation();
        resourceReservation.setReservation(reservation);
        // TODO: fix
        //resourceReservation.setResource(resource);
        return resourceReservation;
    }
}

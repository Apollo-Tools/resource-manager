package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
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

    public ReservationHandler(ReservationService reservationService, ResourceService resourceService,
                              ResourceReservationService resourceReservationService, MetricValueService metricValueService) {
        super(new ReservationChecker(reservationService));
        this.reservationChecker = (ReservationChecker) super.entityChecker;
        this.resourceChecker = new ResourceChecker(resourceService);
        this.resourceReservationChecker = new ResourceReservationChecker(resourceReservationService);
        this.metricValueChecker = new MetricValueChecker(metricValueService);
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return super.getOne(rc)
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

    // TODO: deploy resources
    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        ReserveResourcesRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(ReserveResourcesRequest.class);
        return Completable
                .merge(checkResourcesExistAndAreNotReserved(requestDTO.getResources()))
                .andThen(Single.just(new Reservation()))
                .flatMap(reservation -> {
                    reservation.setActive(true);
                    return entityChecker.submitCreate(JsonObject.mapFrom(reservation));
                })
                .map(reservationJson -> createResourceReservationList(reservationJson, requestDTO.getResources()))
                .flatMap(resourceReservations -> resourceReservationChecker
                        .submitCreateAll(Json.encodeToBuffer(resourceReservations).toJsonArray())
                        .andThen(Single.just(JsonObject.mapFrom(resourceReservations.get(0).getReservation()))));
    }

    // TODO: terminate resources
    @Override
    protected Completable updateOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
                .flatMap(entityChecker::checkFindOne)
                .flatMapCompletable(reservationChecker::submitCancelReservation);
    }

    private Single<List<JsonObject>> checkFindResourcesFromReservation(JsonArray resourceReservations) {
        return Observable.fromStream(resourceReservations.stream())
                .flatMapSingle(resourceReservationObject -> {
                    ResourceReservation resourceReservation = ((JsonObject) resourceReservationObject)
                            .mapTo(ResourceReservation.class);
                    Resource resource = resourceReservation.getResource();
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
        resourceReservation.setResource(resource);
        return resourceReservation;
    }
}

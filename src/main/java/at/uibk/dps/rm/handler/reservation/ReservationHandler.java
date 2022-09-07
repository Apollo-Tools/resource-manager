package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class ReservationHandler extends ValidationHandler {

    private final ResourceChecker resourceChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    public ReservationHandler(ReservationService reservationService, ResourceService resourceService,
                              ResourceReservationService resourceReservationService) {
        super(new ReservationChecker(reservationService));
        this.resourceChecker = new ResourceChecker(resourceService);
        this.resourceReservationChecker = new ResourceReservationChecker(resourceReservationService);
    }

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

    private List<Completable> checkResourcesExistAndAreNotReserved(List<Long> resourceIds) {
        List<Completable> completables = new ArrayList<>();
        resourceIds.forEach(resourceId ->
                completables.add(resourceChecker.checkExistsOneAndIsNotReserved(resourceId)));
        return completables;
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

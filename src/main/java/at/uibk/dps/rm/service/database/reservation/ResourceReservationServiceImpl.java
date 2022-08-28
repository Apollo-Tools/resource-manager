package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.repository.ResourceReservationRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class ResourceReservationServiceImpl extends ServiceProxy<ResourceReservation> implements ResourceReservationService {
    public ResourceReservationServiceImpl(ResourceReservationRepository resourceReservationRepository) {
        super(resourceReservationRepository, ResourceReservation.class);
    }
}

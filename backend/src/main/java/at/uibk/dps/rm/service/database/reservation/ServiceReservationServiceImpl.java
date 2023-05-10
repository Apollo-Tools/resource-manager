package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.reservation.ServiceReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class ServiceReservationServiceImpl  extends DatabaseServiceProxy<ServiceReservation>
    implements ServiceReservationService {
    public ServiceReservationServiceImpl(ServiceReservationRepository repository) {
        super(repository, ServiceReservation.class);
    }
}

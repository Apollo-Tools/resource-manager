package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.repository.reservation.FunctionReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class FunctionReservationServiceImpl  extends DatabaseServiceProxy<FunctionReservation> implements
    FunctionReservationService {
    public FunctionReservationServiceImpl(FunctionReservationRepository repository) {
        super(repository, FunctionReservation.class);
    }
}

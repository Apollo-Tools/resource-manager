package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.ServiceReservation;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.List;

public class ContainerFileService extends TerraformFileService {

    private final long reservationId;

    private final List<ServiceReservation> serviceReservations;

    public ContainerFileService(FileSystem fileSystem, Path rootFolder, List<ServiceReservation> serviceReservations,
                           long reservationId) {
        super(fileSystem, rootFolder);
        this.serviceReservations = serviceReservations;
        this.reservationId = reservationId;
    }

    @Override
    protected String getProviderString() {
        return "";
    }

    @Override
    protected String getMainFileContent() {
        return "";
    }

    @Override
    protected String getCredentialVariablesString() {
        return null;
    }

    @Override
    protected String getVariablesFileContent() {
        return "";
    }

    @Override
    protected String getOutputString() {
        return "";
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}

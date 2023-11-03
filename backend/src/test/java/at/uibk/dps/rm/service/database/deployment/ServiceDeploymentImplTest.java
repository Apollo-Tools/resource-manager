package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Implements tests for the {@link ServiceDeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceDeploymentImplTest {

    private ServiceDeploymentService service;

    @Mock
    ServiceDeploymentRepository repository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceDeploymentServiceImpl(repository, smProvider);
    }
}

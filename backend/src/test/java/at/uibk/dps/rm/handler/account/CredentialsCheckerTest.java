package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Implements tests for the {@link CredentialsChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class CredentialsCheckerTest {

    CredentialsChecker credentialsChecker;

    @Mock
    CredentialsService credentialsService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsChecker = new CredentialsChecker(credentialsService);
    }
}

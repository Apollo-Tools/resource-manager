package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceProviderChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceProviderCheckerTest {

    ResourceProviderChecker resourceProviderChecker;

    @Mock
    ResourceProviderService resourceProviderService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceProviderChecker = new ResourceProviderChecker(resourceProviderService);
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        long providerId = 1L;
        String provider = "ibm";
        ResourceProvider resourceProvider = TestResourceProviderProvider
            .createResourceProvider(providerId, provider);

        when(resourceProviderService.existsOneByProvider(provider))
            .thenReturn(Single.just(false));

        resourceProviderChecker.checkForDuplicateEntity(JsonObject.mapFrom(resourceProvider))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(resourceProviderService).existsOneByProvider(provider);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        long providerId = 1L;
        String provider = "ibm";
        ResourceProvider resourceProvider = TestResourceProviderProvider
            .createResourceProvider(providerId, provider);

        when(resourceProviderService.existsOneByProvider(provider))
            .thenReturn(Single.just(true));

        resourceProviderChecker.checkForDuplicateEntity(JsonObject.mapFrom(resourceProvider))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}

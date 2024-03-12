package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ScrapeTargetHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ScrapeTargetHandlerTest {

    private ScrapeTargetHandler scrapeTargetHandler;

    @Mock
    private ResourceService resourceService;

    private Resource r1, r2, r3;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        scrapeTargetHandler = new ScrapeTargetHandler(resourceService);
        r1 = TestResourceProvider.createResource(1L);
        r2 = TestResourceProvider.createResource(2L);
        r3 = TestResourceProvider.createResource(3L);
    }

    @Test
    void getAllSubResourcesByMainResource(VertxTestContext testContext) {
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3)));

        when(resourceService.findAllScrapeTargets()).thenReturn(Single.just(jsonResult));

        scrapeTargetHandler.getAllScrapeTargets()
            .subscribe(result -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                testContext.completeNow();
            });
    }
}

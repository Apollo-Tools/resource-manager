package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.entity.dto.service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.model.EnvVar;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.VolumeMount;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Implements tests for the {@link ServiceInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceInputHandlerTest {

    @Mock
    private RoutingContext rc;

    String portDuplicate;
    EnvVar envVarDuplicate;
    VolumeMount volumeMountDuplicate;

    @BeforeEach
    void init() {
        portDuplicate = "80:8080";
        envVarDuplicate = TestServiceProvider.createEnvVar(1L);
        volumeMountDuplicate = TestServiceProvider.createVolumeMount(1L);
    }

    @Test
    void validateAddServiceRequestValid() {
        Service service = TestServiceProvider.createService(1L, "service", List.of(), List.of(), List.of());
        JsonObject jsonObject = JsonObject.mapFrom(service);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        ServiceInputHandler.validateAddServiceRequest(rc);

        verify(rc).next();
    }

    @Test
    void validateAddServiceRequestInvalidName() {
        Service service = TestServiceProvider.createService(1L, "Service!", List.of(), List.of(), List.of());
        JsonObject jsonObject = JsonObject.mapFrom(service);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        ServiceInputHandler.validateAddServiceRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ports", "envVars", "volumeMounts"})
    void validateAddServiceRequestDuplicates(String type) {
        Service service = TestServiceProvider.createService(1L, "service",
            type.equals("ports") ? List.of(portDuplicate, portDuplicate) : List.of(),
            type.equals("envVars") ? List.of(envVarDuplicate, envVarDuplicate) : List.of(),
            type.equals("volumeMounts") ? List.of(volumeMountDuplicate, volumeMountDuplicate) : List.of());
        JsonObject body = JsonObject.mapFrom(service);

        RoutingContextMockHelper.mockBody(rc, body);
        ServiceInputHandler.validateAddServiceRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
    }

    @Test
    void validateUpdateServiceRequestValid() {
        UpdateServiceDTO updateServiceDTO = TestDTOProvider.createUpdateServiceDTO(null, null, null);
        JsonObject body = JsonObject.mapFrom(updateServiceDTO);

        RoutingContextMockHelper.mockBody(rc, body);
        ServiceInputHandler.validateUpdateServiceRequest(rc);

        verify(rc).next();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ports", "envVars", "volumeMounts"})
    void validateUpdateServiceRequestDuplicates(String type) {
        UpdateServiceDTO updateServiceDTO = TestDTOProvider.createUpdateServiceDTO(
            type.equals("ports") ? List.of(portDuplicate, portDuplicate) : List.of(),
            type.equals("envVars") ? List.of(envVarDuplicate, envVarDuplicate) : List.of(),
            type.equals("volumeMounts") ? List.of(volumeMountDuplicate, volumeMountDuplicate) : List.of());
        JsonObject body = JsonObject.mapFrom(updateServiceDTO);

        RoutingContextMockHelper.mockBody(rc, body);
        ServiceInputHandler.validateUpdateServiceRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
    }
}

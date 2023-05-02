package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ListResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOType;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SLORequestDeserializerTest {

    private static Stream<Arguments> provideValidJsonObject() {
        JsonObject sloAvailability = JsonObject.mapFrom(TestDTOProvider
            .createServiceLevelObjective("availability", ExpressionType.GT, 0.8));
        JsonObject regions = JsonObject.mapFrom(TestDTOProvider
            .createServiceLevelObjective(SLOType.REGION.getValue(), ExpressionType.EQ, 1L, 2L));
        JsonObject providers = JsonObject.mapFrom(TestDTOProvider
            .createServiceLevelObjective(SLOType.RESOURCE_PROVIDER.getValue(), ExpressionType.EQ, 3L, 4L));
        JsonObject resourceTypes = JsonObject.mapFrom(TestDTOProvider
            .createServiceLevelObjective(SLOType.RESOURCE_TYPE.getValue(), ExpressionType.EQ, 5L));
        JsonObject getOneEnsemble = new JsonObject()
            .put("slos", List.of(sloAvailability, regions, providers, resourceTypes))
            .put("ensemble_id", 1L)
            .put("name", "ensemble")
            .put("resources", List.of(JsonObject.mapFrom(TestResourceProvider.createResource(1L))));
        JsonObject createEnsemble = new JsonObject()
            .put("slos", List.of(sloAvailability, regions, providers, resourceTypes))
            .put("name", "ensemble")
            .put("resources", List.of(JsonObject.mapFrom(new ResourceId(){{setResourceId(1L);}})));
        JsonObject listResourcesBySLOs = new JsonObject()
            .put("slos", List.of(sloAvailability, regions, providers, resourceTypes));

        return Stream.of(
                Arguments.of(getOneEnsemble, GetOneEnsemble.class),
                Arguments.of(createEnsemble, CreateEnsembleRequest.class),
                Arguments.of(listResourcesBySLOs, ListResourcesBySLOsRequest.class)
        );
    }

    private static Stream<Arguments> provideInvalidJsonObject() {
        JsonObject regions = JsonObject.mapFrom(TestDTOProvider
                .createServiceLevelObjective(SLOType.REGION.getValue(), ExpressionType.EQ, "one"));
        JsonObject providers = JsonObject.mapFrom(TestDTOProvider
                .createServiceLevelObjective(SLOType.RESOURCE_PROVIDER.getValue(), ExpressionType.GT, 3L, 4L));
        JsonObject resourceTypes = JsonObject.mapFrom(TestDTOProvider
                .createServiceLevelObjective(SLOType.RESOURCE_TYPE.getValue(), ExpressionType.GT, "one"));

        return Stream.of(
                Arguments.of(new JsonObject().put("slos", List.of(regions))),
                Arguments.of(new JsonObject().put("slos", List.of(providers))),
                Arguments.of(new JsonObject().put("slos", List.of(resourceTypes)))
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidJsonObject")
    public void deserialize(JsonObject jsonObject, Class<SLORequest> requestClass) {
        SLORequest result = jsonObject.mapTo(SLORequest.class);

        assertThat(result.getRegions()).isEqualTo(List.of(1L, 2L));
        assertThat(result.getProviders()).isEqualTo(List.of(3L, 4L));
        assertThat(result.getResourceTypes()).isEqualTo(List.of(5L));
        assertThat(result.getServiceLevelObjectives().size()).isEqualTo(1);
        assertThat(result.getServiceLevelObjectives().get(0).getName()).isEqualTo("availability");
        if (requestClass.equals(GetOneEnsemble.class)) {
            GetOneEnsemble getOneEnsemble = (GetOneEnsemble) result;
            assertThat(getOneEnsemble.getEnsembleId()).isEqualTo(1L);
            assertThat(getOneEnsemble.getName()).isEqualTo("ensemble");
            assertThat(getOneEnsemble.getResources().size()).isEqualTo(1);
            assertThat(getOneEnsemble.getResources().get(0).getResourceId()).isEqualTo(1L);
        } else if (requestClass.equals(CreateEnsembleRequest.class)) {
            CreateEnsembleRequest createEnsembleRequest = (CreateEnsembleRequest) result;
            assertThat(createEnsembleRequest.getName()).isEqualTo("ensemble");
            assertThat(createEnsembleRequest.getResources().size()).isEqualTo(1);
            assertThat(createEnsembleRequest.getResources().get(0).getResourceId()).isEqualTo(1L);
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvalidJsonObject")
    public void deserializeInvalidNonMetrics(JsonObject jsonObject) {
        assertThrows(BadInputException.class, () -> jsonObject.mapTo(SLORequest.class));
    }
}

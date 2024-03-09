package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link SLORequestSerializer} class.
 *
 * @author matthi-g
 */
public class GetOneEnsembleSerializerTest {

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    @Test
    void serialize() {
        GetOneEnsemble ensemble = TestDTOProvider.createGetOneEnsemble();
        // Necessary because ordering of set changes between different runs
        ensemble.getResources().forEach(resource -> {
            resource.setIsLocked(true);
            if (resource.getMetricValues().size() > 1) {
                resource.setMetricValues(Set.of(TestMetricProvider
                    .createMetricValue(1L, 1L, "availability", 0.99)));
            }
        });
        ensemble.setRegions(List.of());
        ensemble.setEnvironments(null);
        JsonObject result = JsonObject.mapFrom(ensemble);

        assertThat(result.encode()).isEqualTo("{\"ensemble_id\":1,\"name\":\"ensemble\",\"resources\":[" +
            "{\"resource_id\":1,\"name\":\"mainresource1\",\"is_lockable\":false,\"created_at\":null,\"updated_at\":" +
            "null,\"metric_values\":[{\"metric_value_id\":1,\"count\":10,\"value_number\":0.99,\"value_string\":" +
            "null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":" +
            "\"Blah\",\"is_slo\":null,\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":" +
            "null},\"created_at\":null,\"updated_at\":null}],\"region\":{\"region_id\":1,\"name\":\"us-east-1\"," +
            "\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"provider_platforms\":[]," +
            "\"environment\":{\"environment_id\":1,\"environment\":\"cloud\",\"created_at\":null},\"created_at\":" +
            "null},\"created_at\":null},\"platform\":null,\"sub_resources\":[],\"is_locked\":true},{" +
            "\"resource_id\":2,\"name\":\"mainresource2\",\"is_lockable\":false,\"created_at\":null," +
            "\"updated_at\":null,\"metric_values\":[{\"metric_value_id\":1,\"count\":10,\"value_string\":" +
            "\"t1.micro\",\"value_bool\":null,\"metric\":{\"metric_id\":3,\"metric\":\"instance-type\"," +
            "\"description\":\"Blah\",\"is_slo\":null,\"metric_type\":{\"metric_type_id\":2,\"type\":\"string\"}," +
            "\"created_at\":null},\"created_at\":null,\"updated_at\":null}],\"region\":{\"region_id\":2,\"name\":" +
            "\"us-west-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"provider_platforms\":[]," +
            "\"environment\":{\"environment_id\":1,\"environment\":\"cloud\",\"created_at\":null},\"created_at\":" +
            "null},\"created_at\":null},\"platform\":{\"platform_id\":1,\"platform\":\"ec2\",\"resource_type\":" +
            "{\"type_id\":11,\"resource_type\":\"faas\",\"created_at\":null},\"created_at\":null}," +
            "\"sub_resources\":[],\"is_locked\":true}],\"created_at\":null,\"updated_at\":null,\"slos\":[{\"name\":" +
            "\"timeout\",\"expression\":\">\",\"value\":[150.0]},{\"name\":\"resource_type\",\"expression\":\"==\"," +
            "\"value\":[5.0]},{\"name\":\"platform\",\"expression\":\"==\",\"value\":[3.0,4.0]},{\"name\":" +
            "\"resource_provider\",\"expression\":\"==\",\"value\":[5.0]}]}");
    }
}

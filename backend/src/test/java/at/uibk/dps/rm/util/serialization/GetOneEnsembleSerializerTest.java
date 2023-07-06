package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link GetOneEnsembleSerializer} class.
 *
 * @author matthi-g
 */
public class GetOneEnsembleSerializerTest {

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    @Test
    public void serialize() {
        GetOneEnsemble ensemble = TestDTOProvider.createGetOneEnsemble();
        ensemble.setRegions(List.of());
        JsonObject result = JsonObject.mapFrom(ensemble);

        assertThat(result.encode()).isEqualTo("{\"ensemble_id\":1,\"name\":\"ensemble\",\"resources\":" +
            "[{\"resource_id\":1,\"created_at\":null,\"updated_at\":null,\"region\":{\"region_id\":1," +
            "\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\"," +
            "\"provider_platforms\":[],\"environment\":null,\"created_at\":null},\"created_at\":null}," +
            "\"platform\":null,\"metric_values\":[{\"metric_value_id\":1,\"count\":10,\"value_number\":200.0," +
            "\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"timeout\"," +
            "\"description\":\"Blah\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":" +
            "null,\"is_monitored\":false},\"created_at\":null,\"updated_at\":null},{\"metric_value_id\":2,\"count\":" +
            "10,\"value_number\":1024.0,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2," +
            "\"metric\":\"memory-size\",\"description\":\"Blah\",\"metric_type\":{\"metric_type_id\":1,\"type\":" +
            "\"number\"},\"created_at\":null,\"is_monitored\":false},\"created_at\":null,\"updated_at\":null}]}," +
            "{\"resource_id\":2,\"created_at\":null,\"updated_at\":null,\"region\":{\"region_id\":2,\"name\":" +
            "\"us-west-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"provider_platforms\":[]," +
            "\"environment\":null,\"created_at\":null},\"created_at\":null},\"platform\":{\"platform_id\":1," +
            "\"platform\":\"ec2\",\"resource_type\":{\"type_id\":11,\"resource_type\":\"faas\",\"created_at\":null}," +
            "\"created_at\":null},\"metric_values\":[{\"metric_value_id\":3,\"count\":10,\"value_string\":" +
            "\"t1.micro\",\"value_bool\":null,\"metric\":{\"metric_id\":3,\"metric\":\"instance-type\"," +
            "\"description\":\"Blah\",\"metric_type\":{\"metric_type_id\":2,\"type\":\"string\"},\"created_at\":" +
            "null,\"is_monitored\":false},\"created_at\":null,\"updated_at\":null},{\"metric_value_id\":2," +
            "\"count\":10,\"value_number\":1024.0,\"value_string\":null,\"value_bool\":null,\"metric\":" +
            "{\"metric_id\":2,\"metric\":\"memory-size\",\"description\":\"Blah\",\"metric_type\":{" +
            "\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":null,\"is_monitored\":false},\"created_at\":" +
            "null,\"updated_at\":null},{\"metric_value_id\":1,\"count\":10,\"value_number\":100.0,\"value_string\":" +
            "null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"timeout\",\"description\":\"Blah\"," +
            "\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":null,\"is_monitored\":false}," +
            "\"created_at\":null,\"updated_at\":null}]}],\"created_at\":null,\"updated_at\":null,\"slos\":[{" +
            "\"name\":\"timeout\",\"expression\":\">\",\"value\":[150.0]},{\"name\":\"environment\",\"expression\":" +
            "\"==\",\"value\":[1.0]},{\"name\":\"resource_type\",\"expression\":\"==\",\"value\":[5.0]},{\"name\":" +
            "\"platform\",\"expression\":\"==\",\"value\":[3.0,4.0]},{\"name\":\"resource_provider\"," +
            "\"expression\":\"==\",\"value\":[5.0]}]}");
    }
}

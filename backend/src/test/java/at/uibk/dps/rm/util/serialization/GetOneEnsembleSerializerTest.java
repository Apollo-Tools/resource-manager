package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(result.encode()).isEqualTo("{\"ensemble_id\":1,\"name\":\"ensemble\",\"resources\":[" +
            "{\"resource_id\":1,\"created_at\":null,\"updated_at\":null,\"resource_type\":{\"type_id\":1," +
            "\"resource_type\":\"cloud\",\"created_at\":null},\"region\":{\"region_id\":1,\"name\":\"us-east-1\"," +
            "\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":null},\"created_at\":null}," +
            "\"metric_values\":null,\"is_self_managed\":false},{\"resource_id\":2,\"created_at\":null," +
            "\"updated_at\":null,\"resource_type\":{\"type_id\":1,\"resource_type\":\"cloud\",\"created_at\":null}," +
            "\"region\":{\"region_id\":1,\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1," +
            "\"provider\":\"aws\",\"created_at\":null},\"created_at\":null},\"metric_values\":null," +
            "\"is_self_managed\":false}],\"created_at\":null,\"updated_at\":null,\"slos\":[{\"name\":\"availability\"," +
            "\"expression\":\">\",\"value\":[0.8]},{\"name\":\"resource_provider\",\"expression\":\"==\"," +
            "\"value\":[1.0,2.0]},{\"name\":\"resource_type\",\"expression\":\"==\",\"value\":[5.0]}]}");
    }
}

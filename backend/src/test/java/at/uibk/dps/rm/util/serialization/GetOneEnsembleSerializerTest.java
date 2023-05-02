package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetOneEnsembleSerializerTest {

    @Test
    public void serialize() {
        GetOneEnsemble ensemble = TestDTOProvider.createGetOneEnsemble();
        ensemble.setRegions(List.of());
        JsonObject result = JsonObject.mapFrom(ensemble);

        assertThat(result.encode()).isEqualTo("{\"ensemble_id\":1,\"name\":\"ensemble\",\"resources\":[" +
                "{\"resourceId\":1,\"createdAt\":null,\"updatedAt\":null,\"resourceType\":{\"typeId\":1,\"" +
                "resourceType\":\"cloud\",\"createdAt\":null},\"region\":{\"regionId\":1,\"name\":\"us-east-1\"," +
                "\"resourceProvider\":{\"providerId\":1,\"provider\":\"aws\",\"createdAt\":null},\"createdAt\":null}," +
                "\"metricValues\":null,\"is_self_managed\":false},{\"resourceId\":2,\"createdAt\":null," +
                "\"updatedAt\":null,\"resourceType\":{\"typeId\":1,\"resourceType\":\"cloud\",\"createdAt\":null}," +
                "\"region\":{\"regionId\":1,\"name\":\"us-east-1\",\"resourceProvider\":{\"providerId\":1," +
                "\"provider\":\"aws\",\"createdAt\":null},\"createdAt\":null},\"metricValues\":null," +
                "\"is_self_managed\":false}],\"slos\":[{\"name\":\"availability\",\"expression\":\">\",\"value\":[0.8]}," +
                "{\"name\":\"resource_provider\",\"expression\":\"==\",\"value\":[1.0,2.0]}," +
                "{\"name\":\"resource_type\",\"expression\":\"==\",\"value\":[5.0]}]}");
    }
}

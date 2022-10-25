package at.uibk.dps.rm.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.vertx.core.json.jackson.DatabindCodec;

public class JsonMapperConfig {
    public static void configJsonMapper() {
        // returns the ObjectMapper used by Vert.x
        ObjectMapper mapper = DatabindCodec.mapper();
        PropertyNamingStrategies.SnakeCaseStrategy namingStrategy = new PropertyNamingStrategies.SnakeCaseStrategy();
        mapper.setPropertyNamingStrategy(namingStrategy);
        // returns the ObjectMapper used by Vert.x when pretty printing JSON
        ObjectMapper prettyMapper = DatabindCodec.prettyMapper();
        prettyMapper.setPropertyNamingStrategy(namingStrategy);
        prettyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}

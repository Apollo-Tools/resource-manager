package at.uibk.dps.rm.util.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.experimental.UtilityClass;

/**
 * Used to configure the global json mapper.
 *
 * @author matthi-g
 */
@UtilityClass
public class JsonMapperConfig {
    /**
     * Configure the global json mapper.
     */
    public static void configJsonMapper() {
        // returns the ObjectMapper used by Vert.x
        ObjectMapper mapper = DatabindCodec.mapper();
        PropertyNamingStrategies.SnakeCaseStrategy namingStrategy = new PropertyNamingStrategies.SnakeCaseStrategy();
        mapper.setPropertyNamingStrategy(namingStrategy);
        mapper.registerModule(new Hibernate5Module());
        // returns the ObjectMapper used by Vert.x when pretty printing JSON
        ObjectMapper prettyMapper = DatabindCodec.prettyMapper();
        prettyMapper.setPropertyNamingStrategy(namingStrategy);
        prettyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}

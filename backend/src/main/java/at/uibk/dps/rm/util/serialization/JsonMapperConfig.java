package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import at.uibk.dps.rm.util.misc.MetricPair;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kubernetes.client.custom.Quantity;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MultiValuedMap;

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
        mapper.registerModule(new JavaTimeModule());
        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(Quantity.class, new QuantityDeserializer());
        customModule.addSerializer(MetricPair.class, new MetricPairSerializer());
        customModule.addSerializer(new MultiValuedMapSerializer<>());
        customModule.addDeserializer(MultiValuedMap.class, new MultiValuedMapDeserializer<>(K8sPod.class));
        mapper.registerModule(customModule);
        // returns the ObjectMapper used by Vert.x when pretty printing JSON
        ObjectMapper prettyMapper = DatabindCodec.prettyMapper();
        prettyMapper.setPropertyNamingStrategy(namingStrategy);
        prettyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}

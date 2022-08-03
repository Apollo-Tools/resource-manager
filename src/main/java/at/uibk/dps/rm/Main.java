package at.uibk.dps.rm;

import at.uibk.dps.rm.verticle.MainVerticle;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.rxjava3.core.Vertx;


public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        configJsonMapper();
        vertx.deployVerticle(new MainVerticle()).blockingSubscribe();
    }

    private static void configJsonMapper() {
        // returns the ObjectMapper used by Vert.x
        ObjectMapper mapper = DatabindCodec.mapper();
        SnakeCaseStrategy namingStrategy = new SnakeCaseStrategy();
        mapper.setPropertyNamingStrategy(namingStrategy);
        // returns the ObjectMapper used by Vert.x when pretty printing JSON
        ObjectMapper prettyMapper = DatabindCodec.prettyMapper();
        prettyMapper.setPropertyNamingStrategy(namingStrategy);
        prettyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}

package at.uibk.dps.rm;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class TestConfig {

    public static JsonObject getConfig() {
        JsonObject config = new JsonObject()
            .put("db_name", "resource-manager")
            .put("db_host", "localhost")
            .put("db_port", 5432)
            .put("db_user", "root")
            .put("db_password", "root")
            .put("api_port", 8888);

        return config;
    }
}

package at.uibk.dps.rm.testutil.objectprovider;

import io.vertx.core.json.JsonObject;

/**
 * Utility class to instantiate vertx config objects.
 *
 * @author matthi-g
 */
public class TestConfigProvider {

    public static JsonObject getConfig() {
        return new JsonObject("{\n\"db_host\": \"localhost\", \"db_port\": 5432, \"db_user\": \"root\"," +
            "\"db_password\": \"root\", \"api_port\": 8888, \"build_directory\": \"build\"," +
            "\"dind_directory\": \"var/lib/apollo-rm/\", \"jwt_secret\": \"test-secret\", \"jwt_algorithm\": " +
            "\"HS256\", \"token_minutes_valid\": 1080}\n");
    }
}

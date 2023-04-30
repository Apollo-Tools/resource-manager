package at.uibk.dps.rm.util.configuration;

import io.reactivex.rxjava3.core.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.Vertx;

/**
 * This class is used to load the vertx config from the environment variables and config file.
 *
 * @author matthi-g
 */
public class ConfigUtility {

    private final ConfigRetriever configRetriever;

    /**
     * Create an instance from vertx.
     *
     * @param vertx the vertx instance
     */
    public ConfigUtility(Vertx vertx) {
        ConfigStoreOptions storeEnv = new ConfigStoreOptions().setType("env");
        ConfigStoreOptions storeFile = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "./conf/config.json"));
        this.configRetriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(storeFile)
            .addStore(storeEnv));
    }

    /**
     * Get the config.
     *
     * @return a Single that emits the config as JsonObject
     */
    public Single<JsonObject> getConfig() {
        return this.configRetriever.getConfig();
    }
}

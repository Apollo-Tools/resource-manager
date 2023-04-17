package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.Vertx;

public class ConfigUtility {

    private final ConfigRetriever configRetriever;

    public ConfigUtility(Vertx vertx) {
        ConfigStoreOptions storeEnv = new ConfigStoreOptions().setType("env");
        ConfigStoreOptions storeFile = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "./conf/config.json"));
        this.configRetriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(storeFile)
            .addStore(storeEnv));
    }

    public Single<JsonObject> getConfig() {
        return this.configRetriever.getConfig();
    }
}

package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DockerHubImageChecker implements DockerImageChecker {

    private final Vertx vertx;

    private final DockerCredentials dockerCredentials;

    @Override
    public Single<Set<Function>> getNecessaryFunctionBuilds(List<FunctionDeployment> functionDeployments) {
        List<Single<Pair<Function, Boolean>>> singles = new ArrayList<>();
        Set<Function> checkedFunctions = new HashSet<>();
        if (!dockerCredentials.getRegistry().equals("docker.io".toLowerCase())) {
            return Observable.fromIterable(functionDeployments)
                .map(FunctionDeployment::getFunction)
                .toList()
                .map(HashSet::new);
        }
        for (FunctionDeployment functionDeployment : functionDeployments) {
            Function function = functionDeployment.getFunction();
            if (checkedFunctions.add(function)) {
                String imageName = dockerCredentials.getUsername() + "/" + function.getFunctionDeploymentId();
                singles.add(isUpToDate(imageName, function.getUpdatedAt())
                    .map(isUpToDate -> new Pair<>(function, !isUpToDate)));
            }
        }
        return Single.zip(singles, objects -> Arrays.stream(objects)
                .map(mapped -> (Pair<Function, Boolean>) mapped)
                .filter(Pair::component2)
                .map(Pair::component1)
                .collect(Collectors.toSet())
        );
    }

    @Override
    public Single<Boolean> isUpToDate(String imageName, String tag, Date lastFunctionUpdate) {
        WebClient client = WebClient.create(vertx);
        String API_HOST = "hub.docker.com";
        String API_PATH = "/v2/repositories/";
        return client.get(API_HOST, API_PATH + imageName + "/tags/" + tag)
            .send()
            .map(response -> {
                if (response.statusCode() == 404) {
                    return false;
                } else if (response.statusCode() != 200) {
                    throw new RuntimeException("docker image validation failed");
                }
                JsonObject body = response.bodyAsJsonObject();
                Date lastPushed = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(body.getString("tag_last_pushed"));
                int isUpToDate = lastPushed.compareTo(lastFunctionUpdate);
                return isUpToDate > 0;
            });
    }

    public Single<Boolean> isUpToDate(String imageName, Timestamp lastFunctionUpdate) {
        return isUpToDate(imageName, "latest", lastFunctionUpdate);
    }
}

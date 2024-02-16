package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link DockerImageChecker} for <a href="https://hub.docker.com">Docker Hub</a>
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class DockerHubImageChecker implements DockerImageChecker {

    private static final Logger logger = LoggerFactory.getLogger(DockerHubImageChecker.class);

    private final Vertx vertx;

    private final DockerCredentials dockerCredentials;

    @Override
    public Single<Set<Function>> getNecessaryFunctionBuilds(List<FunctionDeployment> functionDeployments) {
        List<Single<Pair<Function, Boolean>>> singles = new ArrayList<>();
        Set<Function> checkedFunctions = new HashSet<>();
        if (dockerCredentials == null || !dockerCredentials.getRegistry().equals("docker.io".toLowerCase())) {
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
        String repoUrl = "https://hub.docker.com/v2/repositories/" + imageName + "/tags/" + tag;
        return client.getAbs(repoUrl)
            .send()
            .map(response -> {
                client.close();
                if (response.statusCode() == 404) {
                    return false;
                } else if (response.statusCode() != 200) {
                    throw new RuntimeException("docker image validation failed");
                }
                JsonObject body = response.bodyAsJsonObject();
                String lastPushedTimeStamp = body.getString("tag_last_pushed");
                Date lastPushed = Date.from(Instant.parse(lastPushedTimeStamp));
                int isUpToDate = lastPushed.compareTo(lastFunctionUpdate);
                return isUpToDate > 0;
            })
            .onErrorReturn(throwable -> {
                logger.warn(throwable.getMessage());
                return false;
            });
    }

    /**
     * See isUpToDate(String imageName, String tag, Date lastFunctionUpdate)
     *
     * @param imageName the name of the image
     * @param lastFunctionUpdate the last update of the function
     * @return a Single that emits true if the image is up-to-date, else false
     */
    private Single<Boolean> isUpToDate(String imageName, Timestamp lastFunctionUpdate) {
        return isUpToDate(imageName, "latest", lastFunctionUpdate);
    }
}

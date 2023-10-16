package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.service.deployment.docker.DockerHubImageChecker;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;

/**
 * Utility class to mock (mocked construction) general objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class Mockprovider {

    public static MockedConstruction<ConfigUtility> mockConfig(ConfigDTO config) {
        return Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> {
                given(mock.getConfigJson()).willReturn(Single.just(JsonObject.mapFrom(config)));
                given(mock.getConfigDTO()).willReturn(Single.just(config));
            });
    }


    public static MockedConstruction<DockerHubImageChecker> mockDockerHubImageChecker(
            List<FunctionDeployment> functionDeployments, Set<Function> result) {
        return Mockito.mockConstruction(DockerHubImageChecker.class, (mock, context) ->
            given(mock.getNecessaryFunctionBuilds(functionDeployments)).willReturn(Single.just(result)));
    }

    public static MockedConstruction<WebClient> mockWebClientDockerHubCheck(List<String> imageNames, List<String> tags,
            HttpRequest<Buffer> requestMock) {
        return Mockito.mockConstruction(WebClient.class, (mock, context) -> {
            for(int i = 0; i < imageNames.size(); i++) {
                given(mock.get("hub.docker.com", "/v2/repositories/" + imageNames.get(i) + "/tags/" +
                    tags.get(i))).willReturn(requestMock);
            }
        });
    }
}

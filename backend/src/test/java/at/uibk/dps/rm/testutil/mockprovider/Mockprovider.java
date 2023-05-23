package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.DeployTerminateRequest;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.nio.file.Path;

import static org.mockito.BDDMockito.given;

/**
 * Utility class to mock (mocked construction) objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class Mockprovider {

    public static MockedConstruction<ConfigUtility> mockConfig(JsonObject config) {
        return Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)));
    }

    public static MockedConstruction<DockerImageService> mockDockerImageService(FunctionsToDeploy functionsToDeploy,
            ProcessOutput processOutput) {
        return Mockito.mockConstruction(DockerImageService.class,
            (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                .willReturn(Single.just(processOutput)));
    }

    public static MockedConstruction<MainTerraformExecutor> mockMainTerraformExecutor(DeploymentPath deploymentPath,
            ProcessOutput poInit, ProcessOutput poApply, ProcessOutput poOutput) {
        return Mockito.mockConstruction(MainTerraformExecutor.class,
            (mock, context) -> {
                given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder())).willReturn(Completable.complete());
                given(mock.init(deploymentPath.getRootFolder())).willReturn(Single.just(poInit));
                given(mock.apply(deploymentPath.getRootFolder())).willReturn(Single.just(poApply));
                given(mock.getOutput(deploymentPath.getRootFolder())).willReturn(Single.just(poOutput));
            });
    }

    public static MockedConstruction<MainTerraformExecutor> mockMainTerraformExecutor(DeploymentPath deploymentPath,
            ProcessOutput poDestroy) {
        return Mockito.mockConstruction(MainTerraformExecutor.class,
            (mock, context) -> {
                given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder())).willReturn(Completable.complete());
                given(mock.destroy(deploymentPath.getRootFolder())).willReturn(Single.just(poDestroy));
            });
    }

    private static void mockTerraformExecutor(TerraformExecutor mock, Path path, String mode,
            ProcessOutput processOutput) {
        switch (mode) {
            case "init":
                given(mock.init(path)).willReturn(Single.just(processOutput));
                break;
            case "apply":
                given(mock.apply(path)).willReturn(Single.just(processOutput));
                break;
            case "destroy":
                given(mock.destroy(path)).willReturn(Single.just(processOutput));
                break;
        }
    }


    public static MockedConstruction<TerraformExecutor> mockTerraformExecutor(DeployTerminateRequest request,
            DeploymentPath deploymentPath, ProcessOutput processOutput, String mode) {
        return Mockito.mockConstruction(TerraformExecutor.class,
            (mock, context) -> {
                for (ServiceReservation sr : request.getServiceReservations()) {
                    Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                        String.valueOf(sr.getResourceReservationId()));
                    mockTerraformExecutor(mock, containerPath, mode, processOutput);
                }
            });
    }


    public static MockedConstruction<TerraformExecutor> mockTerraformExecutor(DeploymentPath deploymentPath,
        long resourceReservationId, ProcessOutput processOutput, String mode) {
        return Mockito.mockConstruction(TerraformExecutor.class,
            (mock, context) -> {
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceReservationId));
                mockTerraformExecutor(mock, containerPath, mode, processOutput);
            });
    }
}

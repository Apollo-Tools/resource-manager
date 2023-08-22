package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.service.deployment.docker.LambdaJavaBuildService;
import at.uibk.dps.rm.service.deployment.docker.LambdaLayerService;
import at.uibk.dps.rm.service.deployment.docker.OpenFaasImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.terraform.*;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

/**
 * Utility class to mock (mocked construction) objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class Mockprovider {

    public static MockedConstruction<ConfigUtility> mockConfig(ConfigDTO config) {
        return Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfigDTO()).willReturn(Single.just(config)));
    }

    public static MockedConstruction<OpenFaasImageService> mockDockerImageService(FunctionsToDeploy functionsToDeploy,
            ProcessOutput processOutput) {
        return Mockito.mockConstruction(OpenFaasImageService.class,
            (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                .willReturn(Single.just(processOutput)));
    }

    public static MockedConstruction<MainTerraformExecutor> mockMainTerraformExecutor(DeploymentPath deploymentPath,
            ProcessOutput poInit, ProcessOutput poApply, ProcessOutput poOutput) {
        return Mockito.mockConstruction(MainTerraformExecutor.class,
            (mock, context) -> {
                given(mock.init(deploymentPath.getRootFolder())).willReturn(Single.just(poInit));
                given(mock.apply(deploymentPath.getRootFolder())).willReturn(Single.just(poApply));
                given(mock.getOutput(deploymentPath.getRootFolder())).willReturn(Single.just(poOutput));
            });
    }

    public static MockedConstruction<MainTerraformExecutor> mockMainTerraformExecutor(DeploymentPath deploymentPath,
            ProcessOutput poDestroy) {
        return Mockito.mockConstruction(MainTerraformExecutor.class,
            (mock, context) -> given(mock.destroy(deploymentPath.getRootFolder())).willReturn(Single.just(poDestroy)));
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

    public static MockedConstruction<TerraformExecutor> mockTerraformExecutor(DeployTerminateDTO request,
            DeploymentPath deploymentPath, ProcessOutput processOutput, String mode) {
        return Mockito.mockConstruction(TerraformExecutor.class,
            (mock, context) -> {
                for (ServiceDeployment sr : request.getServiceDeployments()) {
                    Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                        String.valueOf(sr.getResourceDeploymentId()));
                    mockTerraformExecutor(mock, containerPath, mode, processOutput);
                }
            });
    }


    public static MockedConstruction<TerraformExecutor> mockTerraformExecutor(DeploymentPath deploymentPath,
        long resourceDeploymentId, ProcessOutput processOutput, String mode) {
        return Mockito.mockConstruction(TerraformExecutor.class,
            (mock, context) -> {
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceDeploymentId));
                mockTerraformExecutor(mock, containerPath, mode, processOutput);
            });
    }

    public static MockedConstruction<LambdaJavaBuildService> mockLambdaJavaService(ProcessOutput processOutput) {
        return Mockito.mockConstruction(LambdaJavaBuildService.class,
            (mock, context) -> given(mock.buildAndZipJavaFunctions("var/lib/apollo-rm/"))
                .willReturn(Single.just(processOutput)));
    }

    public static MockedConstruction<LambdaLayerService> mockLambdaLayerService(ProcessOutput processOutput) {
        return Mockito.mockConstruction(LambdaLayerService.class,
            (mock, context) -> given(mock.buildLambdaLayers("var/lib/apollo-rm/"))
                .willReturn(Single.just(processOutput)));
    }

    public static MockedConstruction<ProcessExecutor> mockProcessExecutor(DeploymentPath deploymentPath,
            ProcessOutput processOutput, List<String> commands) {
        return Mockito.mockConstruction(ProcessExecutor .class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(commands);
                assertThat(context.arguments().get(0)).isEqualTo(deploymentPath.getRootFolder());
            });
    }

    public static MockedConstruction<ProcessExecutor> mockProcessExecutor(ProcessOutput processOutput) {
        return Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> given(mock.executeCli()).willReturn(Single.just(processOutput)));
    }

    public static MockedConstruction<PackagePythonCode> mockPackagePythonCode() {
        return Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode())
                .willReturn(Completable.complete()));
    }

    public static MockedConstruction<ProcessBuilder> mockProcessBuilderIOException(Path workingDir) {
        return Mockito.mockConstruction(ProcessBuilder.class,
            (mock, context) -> {
                given(mock.directory(workingDir.toFile())).willCallRealMethod();
                given(mock.redirectErrorStream(true)).willCallRealMethod();
                given(mock.start()).willThrow(new IOException());
            });
    }

    public static MockedConstruction<FunctionPrepareService> mockFunctionPrepareService(FunctionsToDeploy functionsToDeploy) {
        return Mockito.mockConstruction(FunctionPrepareService.class,
            (mock, context) -> given(mock.packageCode()).willReturn(Single.just(functionsToDeploy)));
    }

    public static MockedConstruction<TerraformSetupService> mockTFSetupServiceSetupModuleDirs(ConfigDTO config,
            Single<List<TerraformModule>> result) {
        return Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
            given(mock.setUpTFModuleDirs(config)).willReturn(result));
    }

    public static MockedConstruction<TerraformSetupService> mockTFSetupServiceGetTerminationCreds(
            Single<DeploymentCredentials> result) {
        return Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
            given(mock.getTerminationCredentials()).willReturn(result));
    }

    public static MockedConstruction<MainFileService> mockMainFileService(Completable result) {
        return Mockito.mockConstruction(MainFileService.class,
            (mock, context) -> given(mock.setUpDirectory()).willReturn(result));
    }

    public static MockedConstruction<RegionFaasFileService> mockRegionFaasFileService(Completable result) {
        return Mockito.mockConstruction(RegionFaasFileService.class,
            (mock, context) -> given(mock.setUpDirectory()).willReturn(result));
    }

    public static MockedStatic<Vertx> mockVertx() {
        return mockStatic(Vertx.class);
    }
}

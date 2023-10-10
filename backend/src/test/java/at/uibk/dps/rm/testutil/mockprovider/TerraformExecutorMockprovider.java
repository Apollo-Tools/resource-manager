package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import io.reactivex.rxjava3.core.Single;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.BDDMockito.given;

/**
 * Utility class to mock (mocked construction) terraform executor objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class TerraformExecutorMockprovider {
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
            case "output":
                given(mock.getOutput(path)).willReturn(Single.just(processOutput));
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
                                                                              long resourceDeploymentId, ProcessOutput processOutput, String... modes) {
        return Mockito.mockConstruction(TerraformExecutor.class,
            (mock, context) -> {
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceDeploymentId));
                for (String mode: modes) {
                    mockTerraformExecutor(mock, containerPath, mode, processOutput);
                }
            });
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
}

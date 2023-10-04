package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.service.database.util.*;
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
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.core.Vertx;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    public static MockedConstruction<OpenFaasImageService> mockDockerImageService(ProcessOutput processOutput) {
        return Mockito.mockConstruction(OpenFaasImageService.class,
            (mock, context) -> given(mock.buildOpenFaasImages())
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

    public static MockedConstruction<MetricValueUtility> mockMetricValueUtilitySave(SessionManager sm,
            Resource resource, JsonArray data) {
        return Mockito.mockConstruction(MetricValueUtility.class,
            (mock, context) -> given(mock.checkAddMetricList(sm, resource, data)).willReturn(Completable.complete()));
    }

    public static MockedConstruction<K8sResourceUpdateUtility> mockK8sResourceUpdateUtility(SessionManager sm,
            MainResource cluster, K8sMonitoringData data) {
        return Mockito.mockConstruction(K8sResourceUpdateUtility.class,
            (mock, context) -> {
                given(mock.updateClusterNodes(sm, cluster, data)).willReturn(Completable.complete());
                given(mock.updateCluster(sm, cluster, data)).willReturn(Completable.complete());
            });
    }

    public static MockedConstruction<EnsembleUtility> mockEnsembleUtilityFetch(SessionManager sm, long ensembleId,
            long accountId, GetOneEnsemble result) {
        return Mockito.mockConstruction(EnsembleUtility.class, (mock, context) ->
            given(mock.fetchAndPopulateEnsemble(sm, ensembleId, accountId)).willReturn(Single.just(result)));
    }

    public static MockedConstruction<EnsembleUtility> mockEnsembleUtilityFetchAndValidate(SessionManager sm,
            long ensembleId, long accountId, GetOneEnsemble fetchResult, List<Resource> validResources,
            List<ResourceEnsembleStatus> validateResult) {
        return Mockito.mockConstruction(EnsembleUtility.class, (mock, context) -> {
            given(mock.fetchAndPopulateEnsemble(sm, ensembleId, accountId)).willReturn(Single.just(fetchResult));
            given(mock.getResourceEnsembleStatus(validResources, fetchResult.getResources()))
                .willReturn(validateResult);
        });
    }

    public static MockedConstruction<SLOUtility> mockSLOUtilityFindAndFilterResources(SessionManager sm,
            List<Resource> result) {
        return Mockito.mockConstruction(SLOUtility.class, (mock, context) ->
            given(mock.findAndFilterResourcesBySLOs(eq(sm), any(SLORequest.class))).willReturn(Single.just(result)));
    }

    public static MockedConstruction<EnsembleValidationUtility> mockEnsembleValidationUtility(SessionManager sm,
            long ensembleId, long accountId, List<ResourceEnsembleStatus> result) {
        return Mockito.mockConstruction(EnsembleValidationUtility.class, (mock, context) ->
            given(mock.validateAndUpdateEnsemble(sm, ensembleId, accountId)).willReturn(Single.just(result)));
    }

    public static MockedConstruction<EnsembleValidationUtility> mockEnsembleValidationUtilityList(SessionManager sm,
            long accountId, Map<Ensemble, List<ResourceEnsembleStatus>> result) {
        return Mockito.mockConstruction(EnsembleValidationUtility.class, (mock, context) ->
            result.forEach((key, value) -> given(mock.validateAndUpdateEnsemble(sm, key.getEnsembleId(), accountId))
                .willReturn(Single.just(value))));
    }

    public static MockedConstruction<DeploymentUtility> mockDeploymentUtility(SessionManager sm) {
        return Mockito.mockConstruction(DeploymentUtility.class, (mock, context) ->
            given(mock.mapResourceDeploymentsToDTO(eq(sm), any(DeployTerminateDTO.class)))
                .willReturn(Completable.complete()));
    }

    public static MockedConstruction<DeploymentValidationUtility> mockDeploymentValidationUtilityValid(
            SessionManager sm, List<Resource> result) {
        return Mockito.mockConstruction(DeploymentValidationUtility.class, (mock, context) ->
            given(mock.checkDeploymentIsValid(eq(sm), any(DeployResourcesRequest.class), any(DeployResourcesDTO.class)))
                .willReturn(Single.just(result)));
    }

    public static MockedConstruction<SaveResourceDeploymentUtility> mockSaveResourceDeploymentUtility(
            SessionManager sm, ResourceDeploymentStatus status, List<K8sNamespace> namespaces,
            List<Resource> resources) {
        return Mockito.mockConstruction(SaveResourceDeploymentUtility.class, (mock, context) -> {
            given(mock.saveFunctionDeployments(eq(sm), any(Deployment.class), any(DeployResourcesRequest.class),
                eq(status), eq(resources))).willReturn(Completable.complete());
            given(mock.saveServiceDeployments(eq(sm), any(Deployment.class), any(DeployResourcesRequest.class),
                eq(status), eq(namespaces), eq(resources))).willReturn(Completable.complete());
        });
    }

    public static MockedConstruction<TriggerUrlUtility> mockTriggerUrlUtility(SessionManager sm) {
        return Mockito.mockConstruction(TriggerUrlUtility.class, (mock, context) -> {
            given(mock.setTriggerUrlsForFunctions(eq(sm), any(DeploymentOutput.class), any(DeployResourcesDTO.class)))
                .willReturn(Completable.complete());
            given(mock.setTriggerUrlForContainers(eq(sm), any(DeployResourcesDTO.class)))
                .willReturn(Completable.complete());
        });
    }

    public static MockedConstruction<FileOutputStream> mockFileOutputStream(File file) {
        return Mockito.mockConstruction(FileOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(file);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<FileOutputStream> mockFileOutputStream(String path) {
        return Mockito.mockConstruction(FileOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(path);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<ZipOutputStream> mockZipOutputStream() {
        return Mockito.mockConstruction(ZipOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isInstanceOf(FileOutputStream.class);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<ZipOutputStream> mockZipOutputStream(byte[] bytes, int length) {
        return Mockito.mockConstruction(ZipOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isInstanceOf(FileOutputStream.class);
            doNothing().when(mock).write(bytes, 0, length);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<FileInputStream> mockFileInputStream(File file, byte[] bytes) {
        return Mockito.mockConstruction(FileInputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(file);
            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                byte[] arg = ((byte[]) args[0]);
                for (int i = 0; i < arg.length && i < bytes.length; i++) {
                    arg[i] = bytes[i];
                }
                return bytes.length;
            })
                .doAnswer(invocation -> -1)
                .when(mock).read(any());
            doNothing().when(mock).close();
        });
    }


    public static MockedConstruction<FileInputStream> mockFileInputStream(byte[] bytes, File... files) {
        return Mockito.mockConstruction(FileInputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(files[context.getCount()-1]);
            Mockito.lenient().doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                byte[] arg = ((byte[]) args[0]);
                for (int i = 0; i < arg.length && i < bytes.length; i++) {
                    arg[i] = bytes[i];
                }
                return bytes.length;
            })
                .doAnswer(invocation -> -1)
                .when(mock).read(any());
            Mockito.lenient().doNothing().when(mock).close();
        });
    }

    public static MockedStatic<Vertx> mockVertx() {
        return mockStatic(Vertx.class);
    }
}

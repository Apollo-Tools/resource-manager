package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.service.deployment.docker.LambdaJavaBuildService;
import at.uibk.dps.rm.service.deployment.docker.LambdaLayerService;
import at.uibk.dps.rm.service.deployment.docker.OpenFaasImageService;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageJavaCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;

/**
 * Utility class to mock (mocked construction) deployment prepare objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class DeploymentPrepareMockprovider {
    public static MockedConstruction<OpenFaasImageService> mockDockerImageService(ProcessOutput processOutput) {
        return Mockito.mockConstruction(OpenFaasImageService.class,
            (mock, context) -> given(mock.buildOpenFaasImages())
                .willReturn(Single.just(processOutput)));
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

    public static MockedConstruction<PackagePythonCode> mockPackagePythonCode() {
        return Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode())
                .willReturn(Completable.complete()));
    }

    public static MockedConstruction<PackageJavaCode> mockPackageJavaCode() {
        return Mockito.mockConstruction(PackageJavaCode.class,
            (mock, context) -> given(mock.composeSourceCode())
                .willReturn(Completable.complete()));
    }
}

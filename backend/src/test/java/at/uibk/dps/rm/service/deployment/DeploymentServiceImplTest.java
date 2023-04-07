package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.*;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestRequestProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentServiceImplTest {

    private DeploymentService deploymentService;

    @RegisterExtension
    static RunTestOnContext rtoc = new RunTestOnContext();

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        deploymentService = new DeploymentServiceImpl();
    }

    @Test
    void packageFunctionsCode(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();

        try (MockedConstruction<FunctionFileService> ignored = Mockito.mockConstruction(FunctionFileService.class,
            (mock, context) -> given(mock.packageCode()).willReturn(Single.just(functionsToDeploy)))) {
            deploymentService.packageFunctionsCode(deployRequest)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                    assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python39");
                    assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python39");
                    assertThat(result.getFunctionsString().toString()).isEqualTo("\"  foo1_python39:\\n    " +
                        "lang: python3-flask-debian\\n    handler: ./foo1_python39\\n    " +
                        "image: user/foo1_python39:latest\\n  foo2_python39:\\n    lang: python3-flask-debian\\n    " +
                        "handler: ./foo2_python39\\n    image: user/foo2_python39:latest\\n\"");
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void setUpTFModules(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        TerraformModule tfm1 = new TerraformModule(CloudProvider.AWS, "m1");
        TerraformModule tfm2 = new TerraformModule(CloudProvider.EDGE, "m2");

        try (MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
                     given(mock.setUpTFModuleDirs()).willReturn(Single.just(List.of(tfm1, tfm2))))) {
            try (MockedConstruction<MainFileService> ignoredMFS = Mockito.mockConstruction(MainFileService.class,
                (mock, context) -> given(mock.setUpDirectory()).willReturn(Completable.complete()))) {
                deploymentService.setUpTFModules(deployRequest)
                    .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                        assertThat(result.getEdgeLoginCredentials().toString()).isEqualTo("");
                        assertThat(result.getCloudCredentials().size()).isEqualTo(0);
                        testContext.completeNow();
                    })));
            }
        }
    }

    @Test
    void setUpTFModulesMainFileServiceFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        TerraformModule tfm1 = new TerraformModule(CloudProvider.AWS, "m1");
        TerraformModule tfm2 = new TerraformModule(CloudProvider.EDGE, "m2");

        try (MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
                     given(mock.setUpTFModuleDirs()).willReturn(Single.just(List.of(tfm1, tfm2))))) {
            try (MockedConstruction<MainFileService> ignoredMFS = Mockito.mockConstruction(MainFileService.class,
                (mock, context) -> given(mock.setUpDirectory()).willReturn(Completable.error(RuntimeException::new)))) {
                deploymentService.setUpTFModules(deployRequest)
                    .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        testContext.completeNow();
                    })));
            }
        }
    }

    @Test
    void setUpTFModulesTerraformSetupServiceFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();

        try (MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
                     given(mock.setUpTFModuleDirs()).willReturn(Single.error(RuntimeException::new)))) {
            deploymentService.setUpTFModules(deployRequest)
                .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(RuntimeException.class);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void getNecessaryCredentials(VertxTestContext testContext) {
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();

        try (MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
                     given(mock.getDeploymentCredentials()).willReturn(Single.just(deploymentCredentials)))) {
            deploymentService.getNecessaryCredentials(terminateRequest)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getEdgeLoginCredentials().toString())
                        .isEqualTo("edge_login_data=[{auth_user=\\\"user\\\",auth_pw=\\\"pw\\\"},]");
                    assertThat(result.getCloudCredentials().size()).isEqualTo(1);
                    assertThat(result.getCloudCredentials().get(0).getCredentialsId()).isEqualTo(1L);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void getNecessaryCredentialsFailed(VertxTestContext testContext) {
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();

        try (MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockito.mockConstruction(TerraformSetupService.class, (mock, context) ->
                     given(mock.getDeploymentCredentials()).willReturn(Single.error(IllegalStateException::new)))) {
            deploymentService.getNecessaryCredentials(terminateRequest)
                .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IllegalStateException.class);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void deleteTFDirs(VertxTestContext testContext) {
        long reservationId = 1L;
        Path rootFolder = new DeploymentPath(reservationId).getRootFolder();

        try (MockedStatic<TerraformFileService> mockedStatic = Mockito.mockStatic(TerraformFileService.class)) {
            mockedStatic.when(() ->TerraformFileService.deleteAllDirs(any(), eq(rootFolder)))
                .thenReturn(Completable.complete());
            deploymentService.deleteTFDirs(reservationId)
                .onComplete(testContext.succeedingThenComplete());
        }
        testContext.completeNow();
    }

    @Test
    void deleteTFDirsFailed(VertxTestContext testContext) {
        long reservationId = 1L;
        Path rootFolder = new DeploymentPath(reservationId).getRootFolder();

        try (MockedStatic<TerraformFileService> mockedStatic = Mockito.mockStatic(TerraformFileService.class)) {
            mockedStatic.when(() ->TerraformFileService.deleteAllDirs(any(), eq(rootFolder)))
                .thenReturn(Completable.error(IOException::new));
            deploymentService.deleteTFDirs(reservationId)
                .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                })));
        }
        testContext.completeNow();
    }
}

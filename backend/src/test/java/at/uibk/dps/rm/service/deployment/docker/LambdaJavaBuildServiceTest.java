package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import at.uibk.dps.rm.testutil.mockprovider.ProcessExecutorMockprovider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link LambdaJavaBuildService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class LambdaJavaBuildServiceTest {

    private DeploymentPath deploymentPath;
    private FunctionDeployment fd1, fd2, fd3, fd4, fd5, fd6;



    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        ConfigDTO configDTO = TestConfigProvider.getConfigDTO();
        deploymentPath = new DeploymentPath(deployment.getDeploymentId(), configDTO);
        Runtime rJava = TestFunctionProvider.createRuntime(1L, RuntimeEnum.JAVA11.getValue());
        Runtime rPython = TestFunctionProvider.createRuntime(2L, RuntimeEnum.PYTHON38.getValue());

        Function f1 = TestFunctionProvider.createFunction(1L, "func1", "code", rJava);
        Function f2 = TestFunctionProvider.createFunction(2L, "func2", "code", rJava);
        Function f3 = TestFunctionProvider.createFunction(3L, "func3", "code", rPython);
        Resource r1 = TestResourceProvider.createResourceLambda(1L);
        Resource r2 = TestResourceProvider.createResourceOpenFaas(2L, "localhost", "user",
            "pw");
        fd1 = TestFunctionProvider.createFunctionDeployment(1L, f1, r1, false, deployment);
        fd2 = TestFunctionProvider.createFunctionDeployment(2L, f2, r1, false, deployment);
        fd3 = TestFunctionProvider.createFunctionDeployment(3L, f3, r1, false, deployment);
        fd4 = TestFunctionProvider.createFunctionDeployment(4L, f1, r2, false, deployment);
        fd5 = TestFunctionProvider.createFunctionDeployment(5L, f2, r2, false, deployment);
        fd6 = TestFunctionProvider.createFunctionDeployment(6L, f3, r2, false, deployment);
    }

    @Test
    void buildAndZipJavaFunctions(VertxTestContext testContext) {
        LambdaJavaBuildService buildService = new LambdaJavaBuildService(List.of(fd1, fd2, fd3, fd4, fd5, fd6),
            deploymentPath);
        ProcessOutput processOutput = new ProcessOutput();
        String dindFunctionsPath = Path.of("dindDir", deploymentPath.getFunctionsFolder().toString())
            .toAbsolutePath().toString().replace("\\", "/");
        List<String> commands = List.of("docker", "run", "--rm", "-v", dindFunctionsPath + ":/projects", "-w",
            "/projects", "gradle:7-jdk11-jammy", "/bin/sh", "-c",
            "for dir in *_java11;do cd \"$dir\";gradle buildLambdaZip; cd ..;done; exit");

        try(MockedConstruction<ProcessExecutor> ignore = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getFunctionsFolder(), processOutput, commands)) {
            buildService.buildAndZipJavaFunctions("dindDir")
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(processOutput);
                    testContext.completeNow();
                }), throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @Test
    void buildAndZipJavaFunctionsNoLambdaJavaFunction(VertxTestContext testContext) {
        LambdaJavaBuildService buildService = new LambdaJavaBuildService(List.of(fd3, fd4, fd5, fd6), deploymentPath);

        buildService.buildAndZipJavaFunctions("dindDir")
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getProcess()).isEqualTo(null);
                assertThat(result.getOutput()).isEqualTo(null);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}

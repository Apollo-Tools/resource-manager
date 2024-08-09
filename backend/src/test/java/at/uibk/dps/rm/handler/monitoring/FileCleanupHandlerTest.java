package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link FileCleanupHandlerTest} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FileCleanupHandlerTest {

    private FileCleanupHandler cleanupHandler;

    private Vertx spyVertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private DeploymentService deploymentService;

    private static LogCaptor logCaptor;

    private ConfigDTO configDTO;

    @BeforeAll
    static void initAll() {
        logCaptor = LogCaptor.forClass(FileCleanupHandler.class);
    }

    @AfterAll
    static void cleanupAll() {
        logCaptor.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        configDTO = TestConfigProvider.getConfigDTO();
        spyVertx = spy(vertx);
        cleanupHandler = new FileCleanupHandler(spyVertx, configDTO, serviceProxyProvider);
        lenient().when(spyVertx.fileSystem()).thenReturn(fileSystem);
        lenient().when(serviceProxyProvider.getDeploymentService()).thenReturn(deploymentService);
    }

    @AfterEach
    void cleanupEach() {
        logCaptor.clearLogs();
    }

    @Test
    public void startMonitoringLoop(VertxTestContext testContext) throws InterruptedException {
        Deployment d1 = TestDeploymentProvider.createDeployment(1L);
        Deployment d2 = TestDeploymentProvider.createDeployment(3L);
        DeploymentPath dp1 = new DeploymentPath(d1.getDeploymentId(), configDTO);
        DeploymentPath dp2 = new DeploymentPath(d2.getDeploymentId(), configDTO);

        when(fileSystem.readDir(configDTO.getBuildDirectory(), "^deployment_.*"))
            .thenReturn(Single.just(List.of("build\\deployment_1", "build\\deployment_2", "build\\deployment_3")));
        when(deploymentService.findAllWithErrorStateByIds(List.of(1L, 2L, 3L)))
            .thenReturn(Single.just(JsonArray.of(JsonObject.mapFrom(d1), JsonObject.mapFrom(d2))));
        doReturn(Completable.complete()).when(fileSystem).deleteRecursive(dp1.getRootFolder().toString(), true);
        doReturn(Completable.complete()).when(fileSystem).deleteRecursive(dp2.getRootFolder().toString(), true);

        cleanupHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(604800000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: cleanup of build directory",
            "Active deployments: [1, 2, 3]", "Delete deployment directory: 1", "Delete deployment directory: 3",
            "Finished: file cleanup"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopNoDirs(VertxTestContext testContext) throws InterruptedException {
        when(fileSystem.readDir(configDTO.getBuildDirectory(), "^deployment_.*"))
            .thenReturn(Single.just(List.of()));

        cleanupHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(604800000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: cleanup of build directory",
            "Active deployments: []", "Finished: file cleanup"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopNoErrorDirs(VertxTestContext testContext) throws InterruptedException {
        when(fileSystem.readDir(configDTO.getBuildDirectory(), "^deployment_.*"))
            .thenReturn(Single.just(List.of("build/deployment_test", "build/deployment_2", "build/deployment_3")));
        when(deploymentService.findAllWithErrorStateByIds(List.of(-1L, 2L, 3L)))
            .thenReturn(Single.just(new JsonArray()));

        cleanupHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(604800000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: cleanup of build directory",
            "Active deployments: [-1, 2, 3]", "Finished: file cleanup"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopException(VertxTestContext testContext) throws InterruptedException {
        when(fileSystem.readDir(configDTO.getBuildDirectory(), "^deployment_.*"))
            .thenReturn(Single.error(new FileNotFoundException("not found")));

        cleanupHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(604800000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: cleanup of build directory"));
        assertThat(logCaptor.getErrorLogs()).isEqualTo(List.of("not found"));
        testContext.completeNow();
    }


    @Test
    public void pauseMonitoringLoop() {
        cleanupHandler.pauseMonitoringLoop();
        cleanupHandler.pauseMonitoringLoop();

        verify(spyVertx, times(2)).cancelTimer(-1L);
    }
}

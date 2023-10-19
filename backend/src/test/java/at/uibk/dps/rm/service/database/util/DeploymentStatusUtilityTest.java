package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Implements tests for the {@link DeploymentStatusUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentStatusUtilityTest {

    private static Stream<Arguments> provideStatusValue() {
        ResourceDeploymentStatus statusNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
        ResourceDeploymentStatus statusDeployed = TestDeploymentProvider
            .createResourceDeploymentStatusDeployed();
        ResourceDeploymentStatus statusTerminating = TestDeploymentProvider
            .createResourceDeploymentStatusTerminating();
        ResourceDeploymentStatus statusTerminated = TestDeploymentProvider
            .createResourceDeploymentStatusTerminated();
        ResourceDeploymentStatus statusError = TestDeploymentProvider
            .createResourceDeploymentStatusError();
        return Stream.of(
            Arguments.of(statusNew),
            Arguments.of(statusDeployed),
            Arguments.of(statusTerminating),
            Arguments.of(statusTerminated),
            Arguments.of(statusError)
        );
    }

    @ParameterizedTest
    @MethodSource("provideStatusValue")
    void checkCrucialDeploymentStatus(ResourceDeploymentStatus expectedStatus) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(1L, deployment,
            new MainResource(), TestDeploymentProvider.createResourceDeploymentStatusTerminated());
        ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(2L, deployment, new MainResource(),
            expectedStatus);

        DeploymentStatusValue result = DeploymentStatusUtility.checkCrucialResourceDeploymentStatus(List.of(rd1, rd2));

        assertThat(result.name()).isEqualTo(expectedStatus.getStatusValue());
    }

}

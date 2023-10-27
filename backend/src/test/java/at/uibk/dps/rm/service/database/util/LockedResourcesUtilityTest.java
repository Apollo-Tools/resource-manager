package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link LockedResourcesUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class LockedResourcesUtilityTest {

    private LockedResourcesUtility utility;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private SessionManager sessionManager;

    private long deploymentId;
    private Deployment deployment;
    private Resource r1, r2, r3;

    @BeforeEach
    void initTest() {
        utility = new LockedResourcesUtility(resourceRepository);
        deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(2L);
        deployment = TestDeploymentProvider.createDeployment(deploymentId, account);
        r1 = TestResourceProvider.createResource(1L);
        r1.setIsLockable(true);
        r2 = TestResourceProvider.createResource(2L);
        r2.setIsLockable(true);
        r3 = TestResourceProvider.createResource(3L);
        r3.setIsLockable(true);
    }

    @Test
    void lockResources(VertxTestContext testContext) {
        List<ResourceId> resourceIds = TestResourceProvider.createResourceIdsList(1L, 2L, 3L);

        when(resourceRepository.findAllByResourceIdsAndFetch(sessionManager, List.of(1L, 2L, 3L)))
            .thenReturn(Single.just(List.of(r1, r2, r3)));

        utility.lockResources(sessionManager, resourceIds, deployment)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(List.of(r1, r2, r3));
                    result.forEach(resource -> assertThat(resource.getLockedByDeployment()).isEqualTo(deployment));
                    testContext.completeNow();
                }),
                throwable -> testContext.failNow("method has thrown exception")
            );
    }

    @Test
    void lockResourcesNonLockable(VertxTestContext testContext) {
        List<ResourceId> resourceIds = TestResourceProvider.createResourceIdsList(1L, 2L, 3L);
        r2.setIsLockable(false);

        when(resourceRepository.findAllByResourceIdsAndFetch(sessionManager, List.of(1L, 2L, 3L)))
            .thenReturn(Single.just(List.of(r1, r2, r3)));

        utility.lockResources(sessionManager, resourceIds, deployment)
            .subscribe(result -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("resource r2(2) is not lockable");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void unlockResources(VertxTestContext testContext) {
        List<Resource> lockedResources = Stream.of(r1, r2, r3)
            .peek(resource -> resource.setLockedByDeployment(deployment))
            .collect(Collectors.toList());
        when(resourceRepository.findAllLockedByDeploymentId(sessionManager, deploymentId))
            .thenReturn(Single.just(lockedResources));

        utility.unlockDeploymentResources(sessionManager, deploymentId)
            .subscribe(() -> testContext.verify(() -> {
                    lockedResources.forEach(resource -> assertThat(resource.getLockedByDeployment()).isNull());
                    testContext.completeNow();
                }),
                throwable -> testContext.failNow("method has thrown exception")
            );
    }
}

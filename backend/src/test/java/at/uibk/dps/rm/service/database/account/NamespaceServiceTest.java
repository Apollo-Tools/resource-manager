package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link NamespaceServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class NamespaceServiceTest {

    private NamespaceService namespaceService;

    @Mock
    private NamespaceRepository namespaceRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;
    private long accountId;
    private K8sNamespace n1, n2;
    private String clusterName;
    private MainResource cluster;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        namespaceService = new NamespaceServiceImpl(namespaceRepository, resourceRepository, smProvider);
        accountId = 1L;
        clusterName = "cluster";
        cluster = TestResourceProvider.createClusterWithoutNodes(1L, clusterName);
        n1 = TestResourceProviderProvider.createNamespace(1L, "n1", cluster);
        n2 = TestResourceProviderProvider.createNamespace(2L, "n2", cluster);
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(namespaceRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(n1, n2)));

        namespaceService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("namespace_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(0).getJsonObject("resource")).isNotNull();
            assertThat(result.getJsonObject(1).getLong("namespace_id")).isEqualTo(2L);
            assertThat(result.getJsonObject(1).getJsonObject("resource")).isNotNull();
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(namespaceRepository.findAllByAccountIdAndFetch(sessionManager, accountId))
            .thenReturn(Single.just(List.of(n1, n2)));

        namespaceService.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("namespace_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(0).getJsonObject("resource")).isNotNull();
            assertThat(result.getJsonObject(1).getLong("namespace_id")).isEqualTo(2L);
            assertThat(result.getJsonObject(1).getJsonObject("resource")).isNotNull();
            testContext.completeNow();
        })));
    }

    public static Stream<Arguments> provideNamespaceList() {
        return Stream.of(
            Arguments.of(List.of("n1", "n2"), 0, 0),
            Arguments.of(List.of("n1"), 0, 1),
            Arguments.of(List.of("n1", "n3"), 1, 1),
            Arguments.of(List.of("n3", "n4"), 2, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNamespaceList")
    void updateAllClusterNamespaces(List<String> namespaces, int expectedPersistElements,
            int expectedRemoveElements, VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(resourceRepository.findClusterByName(sessionManager, clusterName)).thenReturn(Maybe.just(cluster));
        when(namespaceRepository.findAllByClusterName(sessionManager, clusterName))
            .thenReturn(Single.just(List.of(n1, n2)));
        if (expectedPersistElements > 0) {
            when(sessionManager.persist(any())).thenReturn(Completable.complete());
        }
        if (expectedRemoveElements > 0) {
            when(sessionManager.remove(any())).thenReturn(Completable.complete());
        }

        namespaceService.updateAllClusterNamespaces(clusterName, namespaces,
            testContext.succeeding(result -> testContext.verify(() -> {
                if (expectedPersistElements > 0) {
                    ArgumentCaptor<Object[]> persistArgs = ArgumentCaptor.forClass(Object[].class);
                    verify(sessionManager).persist(persistArgs.capture());
                    List<Object[]> persistCap = persistArgs.getAllValues();
                    assertThat(persistCap.get(0).length).isEqualTo(expectedPersistElements);
                }
                if (expectedRemoveElements > 0) {
                    ArgumentCaptor<Object[]> deleteArgs = ArgumentCaptor.forClass(Object[].class);
                    verify(sessionManager).remove(deleteArgs.capture());
                    List<Object[]> deleteCap = deleteArgs.getAllValues();
                    assertThat(deleteCap.get(0).length).isEqualTo(expectedRemoveElements);
                }
                testContext.completeNow();
            }))
        );
    }
}

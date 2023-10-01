package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link SessionManager} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class SessionManagerTest {

    private SessionManager sessionManager;

    @Mock
    private Session session;

    private long entityId;

    private Resource entity;
    private final Class<Resource> entityClass = Resource.class;

    @BeforeEach
    void initTest() {
        entityId = 1L;
        sessionManager = new SessionManager(session);
        entity = TestResourceProvider.createResource(entityId);
    }

    @Test
    void find(VertxTestContext testContext) {
        when(session.find(entityClass, entityId)).thenReturn(CompletionStages.completedFuture(entity));

        sessionManager.find(entityClass, entityId)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(entity);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void persistOne(VertxTestContext testContext) {
        when(session.persist(entity)).thenReturn(CompletionStages.voidFuture());

        sessionManager.persist(entity)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(entity);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void persistMultiple(VertxTestContext testContext) {
        Resource entity2 = TestResourceProvider.createResource(2L);
        when(session.persist(entity, entity2)).thenReturn(CompletionStages.voidFuture());

        sessionManager.persist(entity, entity2)
            .subscribe(() -> testContext.verify(testContext::completeNow),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void fetch(VertxTestContext testContext) {
        when(session.fetch(entity)).thenReturn(CompletionStages.completedFuture(entity));

        sessionManager.fetch(entity)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(entity);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void mergeOne(VertxTestContext testContext) {
        when(session.merge(entity)).thenReturn(CompletionStages.completedFuture(entity));

        sessionManager.merge(entity)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(entity);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void mergeMultiple(VertxTestContext testContext) {
        Resource entity2 = TestResourceProvider.createResource(2L);
        when(session.merge(entity, entity2)).thenReturn(CompletionStages.voidFuture());

        sessionManager.merge(entity, entity2)
            .subscribe(() -> testContext.verify(testContext::completeNow),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void removeOne(VertxTestContext testContext) {
        when(session.remove(entity)).thenReturn(CompletionStages.voidFuture());

        sessionManager.remove(entity)
            .subscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void removeMultiple(VertxTestContext testContext) {
        Resource entity2 = TestResourceProvider.createResource(2L);
        when(session.remove(entity, entity2)).thenReturn(CompletionStages.voidFuture());

        sessionManager.remove(entity, entity2)
            .subscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void flush(VertxTestContext testContext) {
        when(session.flush()).thenReturn(CompletionStages.voidFuture());

        sessionManager.flush()
            .subscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}

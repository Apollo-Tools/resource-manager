package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.service.database.util.SessionManager;
import lombok.experimental.UtilityClass;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Utility class with different mocking methods of the {@link SessionFactory} and
 * {@link Session}.
 *
 * @author matthi-g
 */
@UtilityClass
public class SessionMockHelper {

    public static SessionManager mockTransaction(SessionFactory sessionFactory, Session session) {
        when(sessionFactory.withTransaction(any(Function.class)))
            .thenAnswer(invocation -> {
                Function<Session, CompletionStage<ResourceType>> function = invocation.getArgument(0);
                return function.apply(session);
            });
        return new SessionManager(session);
    }
}

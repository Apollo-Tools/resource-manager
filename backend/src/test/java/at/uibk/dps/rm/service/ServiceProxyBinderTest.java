package at.uibk.dps.rm.service;

import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.database.account.AccountNamespaceService;
import at.uibk.dps.rm.service.database.account.AccountNamespaceServiceImpl;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceProxyBinder} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceProxyBinderTest {

    @Mock
    private ServiceBinder serviceBinder;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private MessageConsumer<JsonObject> messageConsumer;

    @Test
    void bind() {
        ServiceProxyBinder binder = new ServiceProxyBinder(serviceBinder);
        AccountNamespaceServiceImpl service = new AccountNamespaceServiceImpl(new AccountNamespaceRepository(),
            smProvider);

        when(serviceBinder.setAddress(service.getServiceProxyAddress())).thenReturn(serviceBinder);
        when(serviceBinder.register(AccountNamespaceService.class, service)).thenReturn(messageConsumer);

        binder.bind(AccountNamespaceService.class, service);
    }

}

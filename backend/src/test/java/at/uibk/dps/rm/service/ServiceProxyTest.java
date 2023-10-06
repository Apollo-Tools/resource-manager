package at.uibk.dps.rm.service;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ServiceProxy} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceProxyTest {
    /**
     * Implements a concrete class of the {@link ServiceProxy} class.
     */
    static class ConcreteServiceProxy extends ServiceProxy {}

    @Test
    void getServiceAddress() {
        ServiceProxy serviceProxy = new ConcreteServiceProxy();

        String result = serviceProxy.getServiceProxyAddress();

        assertThat(result).isEqualTo("-service-address");
    }
}

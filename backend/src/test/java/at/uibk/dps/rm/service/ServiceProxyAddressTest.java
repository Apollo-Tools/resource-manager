package at.uibk.dps.rm.service;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ServiceProxyAddress} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceProxyAddressTest {

    @Test
    void getServiceProxyAddressString() {
        String prefix = "prefix";

        String result = ServiceProxyAddress.getServiceProxyAddress(prefix);

        assertThat(result).isEqualTo("prefix-service-address");
    }

    @Test
    void getServiceProxyAddressClass() {
        Class<Integer> entityClass = Integer.class;

        String result = ServiceProxyAddress.getServiceProxyAddress(entityClass);

        assertThat(result).isEqualTo("integer-service-address");
    }

}

package at.uibk.dps.rm.util;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class PasswordUtilityTest {

    private final PasswordUtility passwordUtility = new PasswordUtility();

    @ParameterizedTest
    @ValueSource(strings = {"password1234", "test42", "whatisthis"})
    void hashPassword(String password) {
        char[] givenPassword = password.toCharArray();

        String result = passwordUtility.hashPassword(givenPassword);

        assertThat(givenPassword).doesNotContain(password.toCharArray());
        assertThat(passwordUtility.verifyPassword(result, password.toCharArray()))
            .isEqualTo(true);
    }

    @ParameterizedTest
    @CsvSource({
        "password1234, password1234, true",
        "password1234, test42, false",
        "test42, test42, true",
        "test42, whatisthis, false",
        "whatisthis, whatisthis, true",
        "whatisthis, password1234, false"
    })
    void verifyPassword(String password, String passwordToHash, boolean match) {
        char[] givenPassword = password.toCharArray();
        String hashedPassword = passwordUtility.hashPassword(passwordToHash.toCharArray());

        boolean result = passwordUtility.verifyPassword(hashedPassword, givenPassword);

        assertThat(givenPassword).doesNotContain(password.toCharArray());
        assertThat(result).isEqualTo(match);
    }
}

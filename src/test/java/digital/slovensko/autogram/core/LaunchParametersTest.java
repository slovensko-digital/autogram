package digital.slovensko.autogram.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LaunchParametersTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "localhost",
            "my-custom-local-host",
            "loopback.autogram.slovensko.digital"
    })
        void validateHostAcceptsDotlessOrExplicitLoopbackHost(String host) {
        Assertions.assertEquals(host, LaunchParameters.Validations.validateHost(host));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1",
            "localhost:37200",
            "localhost/path",
            ""
    })
    void validateHostRejectsHostsWithPortsOrPaths(String host) {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LaunchParameters.Validations.validateHost(host));
    }
}
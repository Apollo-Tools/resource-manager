package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import io.reactivex.rxjava3.core.Single;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Utility class to mock (mocked construction) process executor objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class ProcessExecutorMockprovider {
    public static MockedConstruction<ProcessExecutor> mockProcessExecutor(Path path, ProcessOutput processOutput,
                                                                          List<String> commands) {
        return Mockito.mockConstruction(ProcessExecutor .class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(commands);
                assertThat(context.arguments().get(0)).isEqualTo(path);
            });
    }

    public static MockedConstruction<ProcessExecutor> mockProcessExecutor(ProcessOutput processOutput) {
        return Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> given(mock.executeCli()).willReturn(Single.just(processOutput)));
    }

    public static MockedConstruction<ProcessBuilder> mockProcessBuilderIOException(Path workingDir) {
        return Mockito.mockConstruction(ProcessBuilder.class,
            (mock, context) -> {
                given(mock.directory(workingDir.toFile())).willCallRealMethod();
                given(mock.redirectErrorStream(true)).willCallRealMethod();
                given(mock.start()).willThrow(new IOException());
            });
    }
}

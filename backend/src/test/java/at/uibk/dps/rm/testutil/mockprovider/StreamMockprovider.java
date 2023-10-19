package at.uibk.dps.rm.testutil.mockprovider;

import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

/**
 * Utility class to mock (mocked construction) stream objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class StreamMockprovider {
    public static MockedConstruction<FileOutputStream> mockFileOutputStream(File file) {
        return Mockito.mockConstruction(FileOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(file);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<FileOutputStream> mockFileOutputStream(String path) {
        return Mockito.mockConstruction(FileOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(path);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<ZipOutputStream> mockZipOutputStream() {
        return Mockito.mockConstruction(ZipOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isInstanceOf(FileOutputStream.class);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<ZipOutputStream> mockZipOutputStream(byte[] bytes, int length) {
        return Mockito.mockConstruction(ZipOutputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isInstanceOf(FileOutputStream.class);
            doNothing().when(mock).write(bytes, 0, length);
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<FileInputStream> mockFileInputStream(File file, byte[] bytes) {
        return Mockito.mockConstruction(FileInputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(file);
            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                byte[] arg = ((byte[]) args[0]);
                for (int i = 0; i < arg.length && i < bytes.length; i++) {
                    arg[i] = bytes[i];
                }
                return bytes.length;
            })
                .doAnswer(invocation -> -1)
                .when(mock).read(any());
            doNothing().when(mock).close();
        });
    }

    public static MockedConstruction<FileInputStream> mockFileInputStream(byte[] bytes, File... files) {
        return Mockito.mockConstruction(FileInputStream.class, (mock, context) -> {
            assertThat(context.arguments().get(0)).isEqualTo(files[context.getCount()-1]);
            Mockito.lenient().doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                byte[] arg = ((byte[]) args[0]);
                for (int i = 0; i < arg.length && i < bytes.length; i++) {
                    arg[i] = bytes[i];
                }
                return bytes.length;
            })
                .doAnswer(invocation -> -1)
                .when(mock).read(any());
            Mockito.lenient().doNothing().when(mock).close();
        });
    }
}

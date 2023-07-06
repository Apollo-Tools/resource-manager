package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the supported function runtimes.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum RuntimeEnum {
    /**
     * Python 3.8
     */
    PYTHON38("python3.8"),
    /**
     * Java 11
     */
    JAVA11("java11");

    private final String value;

    /**
     * Create an instance from a string runtime. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param runtime the runtime
     * @return the created object
     */
    public static RuntimeEnum fromRuntime(Runtime runtime) {
        return Arrays.stream(RuntimeEnum.values())
            .filter(value -> value.value.equals(runtime.getName()))
            .findFirst()
            .orElseThrow(() -> new RuntimeNotSupportedException("unknown runtime: " + runtime.getName()));
    }

    public String getDotlessValue() {
        return this.value.replace(".","");
    }
}

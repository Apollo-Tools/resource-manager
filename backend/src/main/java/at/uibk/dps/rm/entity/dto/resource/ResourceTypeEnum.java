package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the supported resource types.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum ResourceTypeEnum {
    /**
     * Function as a Service
     */
    FAAS("faas"),
    /**
     * Edge
     */
    EDGE("edge"),
    /**
     * Virtual Machine
     */
    VM("vm"),
    /**
     * Container
     */
    CONTAINER("container");

    private final String value;
}

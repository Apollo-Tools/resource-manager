package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResourceTypeEnum {
    FAAS("faas"),
    EDGE("edge"),
    VM("vm"),
    CONTAINER("container");

    private final String value;
}

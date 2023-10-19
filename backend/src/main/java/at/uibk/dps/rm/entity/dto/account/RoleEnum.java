package at.uibk.dps.rm.entity.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the available roles.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum RoleEnum {
    /**
     * admin
     */
    ADMIN("admin"),
    /**
     * default
     */
    DEFAULT("default");

    private final String value;
}

package at.uibk.dps.rm.entity.deployment.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents type of terraform modules.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum ModuleType {
    /**
     * faas
     */
    FAAS,
    /**
     * service pre pull
     */
    SERVICE_PREPULL,
    /**
     * service deploy
     */
    SERVICE_DEPLOY
}

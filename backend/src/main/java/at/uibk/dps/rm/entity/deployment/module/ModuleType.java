package at.uibk.dps.rm.entity.deployment.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ModuleType {
    /**
     * AWS Lambda
     */
    FAAS,
    /**
     * AWS EC2
     */
    CONTAINER_PREPULL,
    /**
     * OpenFaaS
     */
    CONTAINER_DEPLOY
}

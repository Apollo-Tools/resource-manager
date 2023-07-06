package at.uibk.dps.rm.entity.deployment.module;

import lombok.Getter;

/**
 * Represents a module in the terraform deployment consisting of container deployments.
 *
 * @author matthi-g
 */
@Getter
public class ContainerModule extends TerraformModule {
    /**
     * Create an instance.
     */
    public ContainerModule() {
        super("container", false);
    }
}

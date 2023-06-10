package at.uibk.dps.rm.entity.deployment.module;

import lombok.Getter;

@Getter
public class ContainerModule extends TerraformModule {
    /**
     * Create an instance.
     */
    public ContainerModule() {
        super("container", false);
    }
}

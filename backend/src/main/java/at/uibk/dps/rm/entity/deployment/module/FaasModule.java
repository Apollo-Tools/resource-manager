package at.uibk.dps.rm.entity.deployment.module;

import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import lombok.Getter;

/**
 * Represents a module consisting of faas deployments. For each region a separate module is
 * used during deployment.
 *
 * @author matthi-g
 */
@Getter
public class FaasModule extends TerraformModule {
    private final ResourceProviderEnum resourceProvider;
    private final Region region;

    /**
     * Create an instance from the resourceProvider and the region.
     *
     * @param resourceProvider the resource provider
     * @param region the region of the module
     */
    public FaasModule(ResourceProviderEnum resourceProvider, Region region) {
        super(resourceProvider.getValue() + "_" + region.getName().replace("-", "_"),
            ModuleType.FAAS);
        this.resourceProvider = resourceProvider;
        this.region = region;
    }

    /**
     * Get the functions string of the module that is used in the terraform output.
     *
     * @return the functions string if present, else a blank string
     */
    public String getFunctionsString() {
        return String.format("module.%s.function_output,", this.getModuleName());
    }
}

package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents a module in the terraform deployment. For each region a separate module is
 * used during deployment.
 *
 * @author matthi-g
 */
@Getter()
public class TerraformModule {
    private final CloudProvider cloudProvider;
    private final String moduleName;

    @Setter
    private boolean hasFaas;

    @Setter
    private boolean hasVM;

    @Setter
    private boolean hasEdge;

    /**
     * Create an instance from a cloud provider and module name.
     *
     * @param cloudProvider the cloud provider (AWS, EDGE)
     * @param moduleName the name of the module
     */
    public TerraformModule(final CloudProvider cloudProvider, final String moduleName) {
        this.cloudProvider = cloudProvider;
        this.moduleName = moduleName;
        hasEdge = cloudProvider.equals(CloudProvider.EDGE);
    }

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TerraformModule module = (TerraformModule) obj;
        return moduleName.equals(module.moduleName);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(moduleName);
    }

    /**
     * Get the functions string of the module that is used in the terraform output.
     *
     * @return the functions string if present, else a blank string
     */
    public String getFunctionsString() {
        if (this.hasFaas) {
            return String.format("module.%s.function_urls,", this.moduleName);
        }
        return "";
    }

    /**
     * Get the vm string of the module that is used in the terraform output.
     *
     * @return the vm string if present, else a blank string
     */
    public String getVMString() {
        if (this.hasVM) {
            return String.format("module.%s.vm_urls,", this.moduleName);
        }
        return "";
    }

    /**
     * Get the vm string of the module that is used in the terraform output.
     *
     * @return the edge string if present, else a blank string
     */
    public String getEdgeString() {
        if (this.hasEdge) {
            return "module.edge.edge_urls";
        }
        return "";
    }
}

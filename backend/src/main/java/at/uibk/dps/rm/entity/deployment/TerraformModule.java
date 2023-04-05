package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter()
public class TerraformModule {
    private final CloudProvider cloudProvider;
    private final String moduleName;

    @Setter
    private boolean hasFaas = false;

    @Setter
    private boolean hasVM = false;

    @Setter
    private boolean hasEdge;

    public TerraformModule(CloudProvider cloudProvider, String moduleName) {
        this.cloudProvider = cloudProvider;
        this.moduleName = moduleName;
        hasEdge = cloudProvider.equals(CloudProvider.EDGE);
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraformModule module = (TerraformModule) o;
        return moduleName.equals(module.moduleName);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(moduleName);
    }

    public String getFunctionsString() {
        if (this.hasFaas) {
            return String.format("module.%s.function_urls,", this.moduleName);
        }
        return "";
    }

    public String getVMString() {
        if (this.hasVM) {
            return String.format("module.%s.vm_urls,", this.moduleName);
        }
        return "";
    }

    public String getEdgeString() {
        if (this.hasEdge) {
            return "module.edge.edge_urls";
        }
        return "";
    }
}

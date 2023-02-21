package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter()
@RequiredArgsConstructor
public class TerraformModule {
    private final CloudProvider cloudProvider;
    private final String moduleName;

    @Setter
    private boolean hasFaas = false;

    @Setter
    private boolean hasVM = false;

    @Setter
    private boolean hasEdge = false;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraformModule module = (TerraformModule) o;
        return moduleName.equals(module.moduleName);
    }

    @Override
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
            return String.format("module.%s.edge_data,", this.moduleName);
        }
        return "";
    }
}

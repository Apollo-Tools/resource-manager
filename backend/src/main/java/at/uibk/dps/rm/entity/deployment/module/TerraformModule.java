package at.uibk.dps.rm.entity.deployment.module;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a module in the terraform deployment. For each region a separate module is
 * used during deployment.
 *
 * @author matthi-g
 */
@Getter()
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class TerraformModule {
    private final String moduleName;

    // TODO: remove module type and use instance of
    private final ModuleType moduleType;

    /**
     * Create an instance of the terraform module using the module name and module type.
     *
     * @param moduleName the module name
     * @param moduleType the module type
     */
    public TerraformModule(String moduleName, ModuleType moduleType) {
        this.moduleName = moduleName;
        this.moduleType = moduleType;
    }

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TerraformModule module = (TerraformModule) obj;
        return moduleName.equals(module.moduleName);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(moduleName);
    }
}

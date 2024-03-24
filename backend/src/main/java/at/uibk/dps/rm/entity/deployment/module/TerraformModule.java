package at.uibk.dps.rm.entity.deployment.module;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a module in the terraform deployment. For each region a separate module is
 * used during deployment.
 *
 * @author matthi-g
 */
@Getter()
public class TerraformModule {
    private final String moduleName;

    private final ModuleType moduleType;

    @JsonCreator
    public TerraformModule(@JsonProperty("module_name") String moduleName,
                           @JsonProperty("module_type") ModuleType moduleType) {
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

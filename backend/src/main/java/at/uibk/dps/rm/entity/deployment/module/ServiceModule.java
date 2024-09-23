package at.uibk.dps.rm.entity.deployment.module;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Represents the service terraform module.
 *
 * @author matthi-g
 */
@Getter
public class ServiceModule extends TerraformModule{

    /**
     * Create an instance of the service module using the module name and module type.
     *
     * @param moduleName the module name
     * @param moduleType the module type
     */
    @JsonCreator
    public ServiceModule(@JsonProperty("module_name") String moduleName,
                           @JsonProperty("module_type") ModuleType moduleType) {
        super(moduleName, moduleType);
    }

}

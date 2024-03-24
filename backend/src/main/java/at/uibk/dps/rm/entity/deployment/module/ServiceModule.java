package at.uibk.dps.rm.entity.deployment.module;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ServiceModule extends TerraformModule{
    @JsonCreator
    public ServiceModule(@JsonProperty("module_name") String moduleName,
                           @JsonProperty("module_type") ModuleType moduleType) {
        super(moduleName, moduleType);
    }

}

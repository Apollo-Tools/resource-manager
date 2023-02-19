package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class TerraformModule {
    private final CloudProvider cloudProvider;
    private final String moduleName;

    @Setter
    private String globalOutput = "";

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
}

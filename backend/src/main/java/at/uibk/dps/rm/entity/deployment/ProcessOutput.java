package at.uibk.dps.rm.entity.deployment;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProcessOutput {
    private Process process;

    private String output;
}

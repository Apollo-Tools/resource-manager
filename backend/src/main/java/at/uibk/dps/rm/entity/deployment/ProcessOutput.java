package at.uibk.dps.rm.entity.deployment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Class that is used in the deployment process to store the output of running external processes
 * like terraform, docker or faas-cli.
 *
 * @author matthi-g
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ProcessOutput {
    private Process process;

    private String output;
}

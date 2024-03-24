package at.uibk.dps.rm.entity.deployment.output;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a subsection in the terraform output.
 *
 * @author matthi-g
 */
@Data
@NoArgsConstructor
public class TFOutputContainer {

    private Map<String, TFOutputValueContainer> value = new HashMap<>();
}

package at.uibk.dps.rm.entity.deployment.output;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class TFOutput {

    private Map<String, String> value;
}

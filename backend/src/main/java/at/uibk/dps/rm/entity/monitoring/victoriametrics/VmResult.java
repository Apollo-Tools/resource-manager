package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import at.uibk.dps.rm.util.serialization.VmResultDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = VmResultDeserializer.class)
public class VmResult {

    private Map<String, String> metric;

    private List<Pair<Long, Double>> values;
}

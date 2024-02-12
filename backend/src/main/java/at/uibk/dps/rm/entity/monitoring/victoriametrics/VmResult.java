package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import at.uibk.dps.rm.util.misc.MetricPair;
import at.uibk.dps.rm.util.serialization.VmResultDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents the
 *
 * @author matthi-g
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = VmResultDeserializer.class)
@DataObject
public class VmResult {

    private Map<String, String> metric;

    private List<MetricPair> values;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public VmResult(JsonObject jsonObject) {
        VmResult vmResult = jsonObject.mapTo(VmResult.class);
        this.metric = vmResult.getMetric();
        this.values = vmResult.getValues();
    }

    /**
     * Get the object as a JsonObject.
     *
     * @return the object as JsonObject
     */
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}

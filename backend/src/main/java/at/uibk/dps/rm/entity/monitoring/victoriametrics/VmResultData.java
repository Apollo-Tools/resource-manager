package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents the result data of a {@link VmQuery}.
 *
 * @author matthi-g
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VmResultData {

    @JsonProperty(value = "resultType")
    private String resultType;

    private List<VmResult> result;
}

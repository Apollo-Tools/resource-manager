package at.uibk.dps.rm.entity.dto.credentials.k8s;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the context config in a context entry.
 *
 * @author matthi-g
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ContextConfig {

    String cluster;

    String namespace;
}

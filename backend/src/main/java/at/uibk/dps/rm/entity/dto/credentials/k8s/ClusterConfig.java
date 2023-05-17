package at.uibk.dps.rm.entity.dto.credentials.k8s;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ClusterConfig {
    String server;
}

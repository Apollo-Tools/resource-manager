package at.uibk.dps.rm.entity.dto.credentials.k8s;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an entry in the cluster list of a kube config.
 *
 * @author matthi-g
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Cluster {

    String name;

    ClusterConfig cluster;
}

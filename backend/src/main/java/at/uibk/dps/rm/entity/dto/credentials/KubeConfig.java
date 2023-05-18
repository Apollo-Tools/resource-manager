package at.uibk.dps.rm.entity.dto.credentials;

import at.uibk.dps.rm.entity.dto.credentials.k8s.Cluster;
import at.uibk.dps.rm.entity.dto.credentials.k8s.Context;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the necessary details of a kube config to extract the right namespaces
 * and contexts.
 *
 * @author matthi-g
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class KubeConfig {
    List<Cluster> clusters;

    List<Context> contexts;
}

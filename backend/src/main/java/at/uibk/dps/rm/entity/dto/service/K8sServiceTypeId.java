package at.uibk.dps.rm.entity.dto.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the k8s service type id that is part of update service request.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@Setter
@Getter
public class K8sServiceTypeId {
    private long serviceTypeId;
}

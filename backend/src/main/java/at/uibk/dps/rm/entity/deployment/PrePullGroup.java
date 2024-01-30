package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents the k8s details of a group of images that have to be pre-pulled
 * for future deployment.
 *
 * @author matthi-g
 */
@Getter
@Setter
@RequiredArgsConstructor
public class PrePullGroup {

    private final Long resourceId;

    private final String context;

    private final String namespace;

    private final long timeout;

    private final String nodeName;

    private final String mainResourceName;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrePullGroup prePullGroup = (PrePullGroup) obj;
        return resourceId.equals(prePullGroup.resourceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceId.hashCode();
    }

}

package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the k8s details of a group of images that have to be pre-pulled
 * for future deployment.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class PrePullGroup {

    /**
     * Create an instance from the resourceId, context, namespace and timeout.
     *
     * @param resourceId the id of the resource
     * @param context the k8s context
     * @param namespace the k8s namespace
     * @param timeout the timeout of the pre-pull deployment
     * @param hostname the hostname label of the node
     * @param mainResourceName the name of the main resource
     */
    public PrePullGroup(Long resourceId, String context, String namespace, long timeout, String hostname,
            String mainResourceName) {
        this.resourceId = resourceId;
        this.context = context;
        this.namespace = namespace;
        this.timeout = timeout;
        this.hostname = hostname;
        this.mainResourceName = mainResourceName;
    }

    private final Long resourceId;

    private final String context;

    private final String namespace;

    private final long timeout;

    private final String hostname;

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

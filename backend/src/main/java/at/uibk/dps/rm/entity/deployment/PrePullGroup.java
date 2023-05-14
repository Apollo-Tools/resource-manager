package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrePullGroup {

    public PrePullGroup(Long resourceId, String context, String namespace, long timeout) {
        this.identifier = resourceId + namespace;
        this.context = context;
        this.namespace = namespace;
        this.timeout = timeout;
    }

    private final String identifier;

    private final String context;

    private final String namespace;

    private final long timeout;

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
        return identifier.equals(prePullGroup.identifier);
    }

    @Override
    @Generated
    public int hashCode() {
        return identifier.hashCode();
    }

}

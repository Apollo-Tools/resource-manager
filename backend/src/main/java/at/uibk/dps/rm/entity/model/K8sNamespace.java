package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class K8sNamespace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long namespaceId;

    private String namespace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        K8sNamespace namespace = (K8sNamespace) obj;
        return namespaceId.equals(namespace.namespaceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return namespaceId.hashCode();
    }
}

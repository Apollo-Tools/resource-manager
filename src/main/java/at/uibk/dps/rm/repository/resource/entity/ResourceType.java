package at.uibk.dps.rm.repository.resource.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer typeId;

    private String resourceType;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    public Integer getType_id() {
        return typeId;
    }

    public String getResource_type() {
        return resourceType;
    }

    public void setResource_type(String resourceType) {
        this.resourceType = resourceType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ResourceType that = (ResourceType) o;

        return typeId.equals(that.typeId);
    }

    @Override public int hashCode() {
        return typeId.hashCode();
    }
}

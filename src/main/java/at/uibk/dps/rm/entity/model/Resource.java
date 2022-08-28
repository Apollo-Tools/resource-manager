package at.uibk.dps.rm.entity.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Entity
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @JsonAlias("is_self_managed")
    private Boolean isSelfManaged = false;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type")
    private ResourceType resourceType;

    @OneToMany(mappedBy="resource")
    private Set<MetricValue> metricValues;

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    @JsonGetter("is_self_managed")
    public Boolean getSelfManaged() {
        return isSelfManaged;
    }

    @JsonSetter("is_self_managed")
    public void setSelfManaged(Boolean isSelfManaged) {
        this.isSelfManaged = isSelfManaged;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Set<MetricValue> getMetricValues() {
        return metricValues;
    }

    public void setMetricValues(Set<MetricValue> metricValues) {
        this.metricValues = metricValues;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Resource resource = (Resource) o;

        return resourceId.equals(resource.resourceId);
    }

    @Override public int hashCode() {
        return resourceId.hashCode();
    }
}

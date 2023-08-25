package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.usertypes.StringArrayType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the service entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private ServiceType serviceType;

    @JsonProperty("is_public")
    private Boolean isPublic;

    private String image;

    private Integer replicas;

    @Convert(converter = StringArrayType.class)
    @Column(columnDefinition = "_text")
    private List<String> ports = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(precision = 3, scale = 3)
    private BigDecimal cpu;

    private Integer memory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id")
    private K8sServiceType k8sServiceType;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnvVar> envVars = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VolumeMount> volumeMounts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Account createdBy;

    @Column(insertable = false, updatable = false)
    private  @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private  @Setter(AccessLevel.NONE) Timestamp updatedAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Service that = (Service) obj;
        return serviceId.equals(that.serviceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return serviceId.hashCode();
    }

    public void setEnvVars(List<EnvVar> envVars) {
        this.envVars.clear();
        this.envVars.addAll(envVars.stream()
            .peek(envVar -> envVar.setService(this))
            .collect(Collectors.toList()));
    }

    public void setVolumeMounts(List<VolumeMount> volumeMounts) {
        this.volumeMounts.clear();
        this.volumeMounts.addAll(volumeMounts.stream()
            .peek(volumeMount -> volumeMount.setService(this))
            .collect(Collectors.toList()));
    }
}

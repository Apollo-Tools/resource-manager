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
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long credentialsId;

    private String accessKey;

    private String secretAccessKey;

    private String sessionToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ResourceProvider resourceProvider;

    @Column(insertable = false, updatable = false)
    private @Setter(value = AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;

        return credentialsId.equals(that.credentialsId);
    }

    @Override
    @Generated
    public int hashCode() {
        return credentialsId.hashCode();
    }
}

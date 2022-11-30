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
public class AccountCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountCredentialsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credentials_id")
    private Credentials credentials;

    @Column(insertable = false, updatable = false)
    private  @Setter(value = AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountCredentials that = (AccountCredentials) o;

        return accountCredentialsId.equals(that.accountCredentialsId);
    }

    @Override
    @Generated
    public int hashCode() {
        return accountCredentialsId.hashCode();
    }
}

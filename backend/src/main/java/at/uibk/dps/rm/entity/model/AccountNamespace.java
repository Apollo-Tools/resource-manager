package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the account_namespace entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class AccountNamespace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountNamespaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_id")
    private K8sNamespace namespace;

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
        AccountNamespace an = (AccountNamespace) obj;
        return accountNamespaceId.equals(an.accountNamespaceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return accountNamespaceId.hashCode();
    }
}

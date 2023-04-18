package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    private String username;

    private String password;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Account account = (Account) obj;
        return accountId.equals(account.accountId);
    }

    @Override
    @Generated
    public int hashCode() {
        return accountId.hashCode();
    }
}

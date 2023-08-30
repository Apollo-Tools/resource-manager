package at.uibk.dps.rm.entity.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DTO that represents the data from the create account operation.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@Setter
@Getter
public class NewAccountDTO {
    private String username;
    private String password;
}

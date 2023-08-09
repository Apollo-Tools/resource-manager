package at.uibk.dps.rm.entity.dto.function;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DTO that represents the data from the update function operation.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@Setter
@Getter
public class UpdateFunctionDTO {

    private String code;

    private Boolean isFile;

    private Short timeoutSeconds;

    private Short memoryMegabytes;
}

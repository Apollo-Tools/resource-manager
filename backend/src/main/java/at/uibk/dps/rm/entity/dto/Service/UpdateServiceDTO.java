package at.uibk.dps.rm.entity.dto.Service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * A DTO that represents the data from the update service opertion.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@Setter
@Getter
public class UpdateServiceDTO {

    private Integer replicas;

    private List<String> ports;

    private BigDecimal cpu;

    private Integer memory;

    private ServiceTypeId serviceType;
}

package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents the aws_price entity.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class AWSPrice {

    private Region region;

    private Platform platform;

    private String instanceType;

    private BigDecimal price;
}

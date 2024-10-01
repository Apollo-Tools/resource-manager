package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceProduct;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceTermPriceDimensions;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A utility class to compute the expected price for different resource types.
 *
 * @author matthi-g
 */
public class ComputePriceUtility {

    private static final long REQUEST_AMOUNT = 1_000_000L;

    private static final double REQUEST_DURATION_SECONDS = 0.1;

    /**
     * Compute the price for an EC2 resource.
     *
     * @param priceDimensions the AWS price list price dimensions
     * @return the computed price
     */
    public BigDecimal computeEC2Price(AWSPriceTermPriceDimensions priceDimensions) {
        BigDecimal totalExecTimeHours = BigDecimal.valueOf(REQUEST_AMOUNT * REQUEST_DURATION_SECONDS)
            .divide(BigDecimal.valueOf(3600), RoundingMode.HALF_UP);
        return priceDimensions.getPricePerUnit().getUsd().multiply(totalExecTimeHours);
    }

    /**
     * Compute the price for an AWS Lambda resource.
     *
     * @param priceDimensions the AWS price list price dimensions
     * @return the computed price
     */
    public BigDecimal computeLambdaPrice(AWSPriceProduct product, AWSPriceTermPriceDimensions priceDimensions) {
        if (product.getAttributes().getUsagetype().endsWith("Lambda-GB-Second")) {
            long memoryGB = 1;
            BigDecimal factor = BigDecimal
                .valueOf(REQUEST_AMOUNT * REQUEST_DURATION_SECONDS * memoryGB);
            return priceDimensions.getPricePerUnit().getUsd().multiply(factor);
        } else if (product.getAttributes().getUsagetype().endsWith("Request")) {
            return priceDimensions.getPricePerUnit().getUsd()
                .multiply(BigDecimal.valueOf(REQUEST_AMOUNT));
        }
        return BigDecimal.ZERO;
    }
}

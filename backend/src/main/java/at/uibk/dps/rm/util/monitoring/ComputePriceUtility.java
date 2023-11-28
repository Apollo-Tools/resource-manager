package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceProduct;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceTermPriceDimensions;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class ComputePriceUtility {

    private static final long REQUEST_AMOUNT = 1_000_000L;

    private static final double REQUEST_DURATION_SECONDS = 0.1;

    public static BigDecimal computerEC2Price(AWSPriceTermPriceDimensions term) {
        BigDecimal totalExecTimeHours = BigDecimal.valueOf(REQUEST_AMOUNT * REQUEST_DURATION_SECONDS)
            .divide(BigDecimal.valueOf(3600), RoundingMode.HALF_UP);
        return term.getPricePerUnit().getUsd().multiply(totalExecTimeHours);
    }

    public static BigDecimal computeLambdaPrice(AWSPriceProduct product, AWSPriceTermPriceDimensions term) {
        if (product.getAttributes().getUsagetype().endsWith("Lambda-GB-Second")) {
            long memoryGB = 1;
            BigDecimal factor = BigDecimal
                .valueOf(REQUEST_AMOUNT * REQUEST_DURATION_SECONDS * memoryGB);
            return term.getPricePerUnit().getUsd().multiply(factor);
        } else if (product.getAttributes().getUsagetype().endsWith("Request")) {
            return term.getPricePerUnit().getUsd()
                .multiply(BigDecimal.valueOf(REQUEST_AMOUNT));
        }
        return BigDecimal.ZERO;
    }
}

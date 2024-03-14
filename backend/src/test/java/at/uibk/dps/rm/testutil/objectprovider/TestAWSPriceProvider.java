package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.monitoring.AWSPrice;
import kotlin.Pair;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * Utility class to instantiate objects that are linked to the aws price list.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestAWSPriceProvider {

    public static AWSPrice createAWSPrice(Pair<String, BigDecimal> pricePair, Region region, Platform platform) {
        AWSPrice awsPrice = new AWSPrice();
        awsPrice.setRegion(region);
        awsPrice.setInstanceType(pricePair.component1());
        awsPrice.setPrice(pricePair.component2());
        awsPrice.setPlatform(platform);
        return awsPrice;
    }
}

package at.uibk.dps.rm.service.monitoring.aws;

import io.reactivex.rxjava3.core.Single;
import kotlin.Pair;

import java.math.BigDecimal;
import java.util.List;

/**
 * An interface to implement the computation of aws prices.
 *
 * @author matthi-g
 */
public interface AWSPriceMonitoring {

    /**
     * Compute the expected price by collecting the data from the provided priceUrl.
     *
     * @param priceUrl aws price list api url
     * @return a Single that emits a List of instance type - price pairs.
     */
    Single<List<Pair<String, BigDecimal>>> computeExpectedPrice(String priceUrl);
}

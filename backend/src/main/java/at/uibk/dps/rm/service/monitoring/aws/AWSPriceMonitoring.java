package at.uibk.dps.rm.service.monitoring.aws;

import io.reactivex.rxjava3.core.Single;
import kotlin.Pair;

import java.math.BigDecimal;
import java.util.List;

public interface AWSPriceMonitoring {
    Single<List<Pair<String, BigDecimal>>> computeExpectedPrice(String priceUrl);
}

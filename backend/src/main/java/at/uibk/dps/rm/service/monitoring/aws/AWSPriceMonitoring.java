package at.uibk.dps.rm.service.monitoring.aws;

import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.util.List;

public interface AWSPriceMonitoring {
    Single<List<BigDecimal>> computeExpectedPrice(String priceUrl);
}

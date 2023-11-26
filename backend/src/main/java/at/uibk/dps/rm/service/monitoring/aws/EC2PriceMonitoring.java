package at.uibk.dps.rm.service.monitoring.aws;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class EC2PriceMonitoring implements AWSPriceMonitoring {

    private final WebClient webClient;

    @Override
    public Single<List<BigDecimal>> computeExpectedPrice(String priceUrl) {
        return webClient.getAbs(priceUrl)
            .send()
            .map(response -> {
                return List.of();
            });
    }
}

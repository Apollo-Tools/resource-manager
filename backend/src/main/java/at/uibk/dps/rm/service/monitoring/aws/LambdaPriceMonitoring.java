package at.uibk.dps.rm.service.monitoring.aws;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceList;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class LambdaPriceMonitoring implements AWSPriceMonitoring {

    private final WebClient webClient;

    @Override
    public Single<List<BigDecimal>> computeExpectedPrice(String priceUrl) {
        return webClient.getAbs(priceUrl)
            .send()
            .flatMap(response -> {
                AWSPriceList result = response.bodyAsJsonObject().mapTo(AWSPriceList.class);
                return Observable.fromIterable(result.getProducts().values())
                    .filter(product -> {
                        List<String> groups = List.of("AWS-Lambda-Duration", "AWS-Lambda-Requests");
                        return groups.contains(product.getAttributes().getGroup()) &&
                            !product.getAttributes().getUsagetype().startsWith("Global-");
                    })
                    .flatMapSingle(product -> Observable.fromIterable(result.getTerms().getOnDemand()
                            .get(product.getSku()).values().iterator().next().getPriceDimensions().values())
                        .filter(term -> term.getBeginRange().equals("0"))
                        .firstOrError()
                        .map(term -> {
                            long requestAmount = 1_000_000L;
                            if (product.getAttributes().getUsagetype().endsWith("Lambda-GB-Second")) {
                                double requestDurationSeconds = 0.1;
                                long memoryGB = 1;
                                BigDecimal factor = BigDecimal
                                    .valueOf(requestAmount * requestDurationSeconds * memoryGB);
                                return term.getPricePerUnit().getUsd().multiply(factor);
                            } else if (product.getAttributes().getUsagetype().endsWith("Request")) {
                                return term.getPricePerUnit().getUsd()
                                    .multiply(BigDecimal.valueOf(requestAmount));
                            }
                            return BigDecimal.ZERO;
                        })
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .map(List::of);
            });
    }
}

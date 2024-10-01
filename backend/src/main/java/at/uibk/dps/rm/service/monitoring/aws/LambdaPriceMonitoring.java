package at.uibk.dps.rm.service.monitoring.aws;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceList;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceProduct;
import at.uibk.dps.rm.util.monitoring.ComputePriceUtility;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implements the {@link AWSPriceMonitoring} interface to compute expected prices for AWS Lambda.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class LambdaPriceMonitoring implements AWSPriceMonitoring {

    private final WebClient webClient;

    private final ComputePriceUtility computePriceUtility;

    @Override
    public Single<List<Pair<String, BigDecimal>>> computeExpectedPrice(String priceUrl) {
        return webClient.getAbs(priceUrl)
            .send()
            .flatMapMaybe(response -> Vertx.currentContext().<AWSPriceList>executeBlocking(fut -> {
                JsonObject result = response.bodyAsJsonObject();
                fut.complete(result.mapTo(AWSPriceList.class));
            }))
            .flatMapSingle(priceList -> Observable.fromIterable(priceList.getProducts().values())
                .filter(this::isSupportedLambdaInstance)
                .flatMapSingle(product -> Observable.fromIterable(priceList.getTerms().getOnDemand()
                        .get(product.getSku()).values().iterator().next().getPriceDimensions().values())
                    .filter(term -> term.getBeginRange().equals("0"))
                    .firstOrError()
                    .map(term -> computePriceUtility.computeLambdaPrice(product, term))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .map(price -> List.of(new Pair<>("lambda", price))))
            .toSingle();
    }

    private boolean isSupportedLambdaInstance(AWSPriceProduct product) {
        List<String> groups = List.of("AWS-Lambda-Duration", "AWS-Lambda-Requests");
        return groups.contains(product.getAttributes().getGroup()) &&
            !product.getAttributes().getUsagetype().startsWith("Global-");
    }
}

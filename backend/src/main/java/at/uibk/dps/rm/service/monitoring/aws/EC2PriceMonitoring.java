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
 * Implements the {@link AWSPriceMonitoring} interface to compute expected prices for EC2 instances.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class EC2PriceMonitoring implements AWSPriceMonitoring {

    private final WebClient webClient;

    @Override
    public Single<List<Pair<String, BigDecimal>>> computeExpectedPrice(String priceUrl) {
        return webClient.getAbs(priceUrl)
            .send()
            .flatMapMaybe(response -> Vertx.currentContext().<AWSPriceList>executeBlocking(fut -> {
                JsonObject result = response.bodyAsJsonObject();
                fut.complete(result.mapTo(AWSPriceList.class));
            }))
            .flatMapSingle(priceList -> Observable.fromIterable(priceList.getProducts().values())
                .filter(EC2PriceMonitoring::isSupportedEc2Instance)
                .flatMapSingle(product -> Observable.fromIterable(priceList.getTerms().getOnDemand()
                        .get(product.getSku()).values().iterator().next().getPriceDimensions().values())
                    .filter(term -> term.getBeginRange().equals("0"))
                    .firstOrError()
                    .map(ComputePriceUtility::computerEC2Price)
                    .map(price -> new Pair<>(product.getAttributes().getInstanceType(), price))
                )
                .toList()
            )
            .toSingle();
    }

    private static boolean isSupportedEc2Instance(AWSPriceProduct product) {
        return product.getProductFamily().equals("Compute Instance") &&
            product.getAttributes().getInstancesku() == null &&
            product.getAttributes().getOperatingSystem().equals("Linux") &&
            product.getAttributes().getPreInstalledSw().equals("NA") &&
            product.getAttributes().getTenancy().equals("Shared");
    }
}

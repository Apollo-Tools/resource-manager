package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.*;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.monitoring.AWSPrice;
import kotlin.Pair;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Map;

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

    public static AWSPriceProductAttributes createProductAttributes(String operatingSystem, String instanceType,
            String instanceSku, String preInstalledSw, String tenancy, String usagetype) {
        AWSPriceProductAttributes attributes = new AWSPriceProductAttributes();
        attributes.setOperatingSystem(operatingSystem);
        attributes.setInstanceType(instanceType);
        attributes.setInstancesku(instanceSku);
        attributes.setPreInstalledSw(preInstalledSw);
        attributes.setTenancy(tenancy);
        attributes.setUsagetype(usagetype);
        return attributes;
    }

    public static AWSPriceProduct createAWSPriceProduct(String productFamily, String instanceType,
            String operatingSystem, String instanceSku, String tenancy, String preInstalledSw, String usageType) {
        AWSPriceProduct awsPriceProduct = new AWSPriceProduct();
        awsPriceProduct.setProductFamily(productFamily);
        awsPriceProduct.setAttributes(createProductAttributes(operatingSystem, instanceType, instanceSku,
            preInstalledSw, tenancy, usageType));
        awsPriceProduct.setSku(productFamily + "." + productFamily);
        return awsPriceProduct;
    }

    public static AWSPriceList createAWSPriceList(Map<String, AWSPriceProduct> productMap, AWSPriceTerms terms) {
        AWSPriceList awsPriceList = new AWSPriceList();
        awsPriceList.setProducts(productMap);
        awsPriceList.setTerms(terms);
        return awsPriceList;
    }

    public static AWSPriceTerm createAwsPriceTerm(String dimension, BigDecimal price) {
        AWSPriceTerm awsPriceTerm = new AWSPriceTerm();
        AWSPricePricePerUnit pricePerUnit = new AWSPricePricePerUnit();
        pricePerUnit.setUsd(price);
        AWSPriceTermPriceDimensions priceDimensions = new AWSPriceTermPriceDimensions();
        priceDimensions.setPricePerUnit(pricePerUnit);
        priceDimensions.setBeginRange("0");
        awsPriceTerm.setPriceDimensions(Map.of(dimension, priceDimensions));
        return awsPriceTerm;
    }

    public static AWSPriceTerms createAWSPriceTerms(Map<String, Map<String, AWSPriceTerm>> onDemand) {
        AWSPriceTerms awsPriceTerms = new AWSPriceTerms();
        awsPriceTerms.setOnDemand(onDemand);
        return awsPriceTerms;
    }
}

package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * A DTO that represents a single AWS price list product.
 *
 * @author matthi-g
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceProduct {

    private String sku;

    @JsonAlias(value = "productFamily")
    private String productFamily;

    private AWSPriceProductAttributes attributes;
}

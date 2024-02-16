package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * A DTO that represents a single AWS price list term.
 *
 * @author matthi-g
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceTerm {

    @JsonAlias(value = "offerTermCode")
    private String offerTermCode;

    @JsonAlias(value = "priceDimensions")
    private Map<String, AWSPriceTermPriceDimensions> priceDimensions;
}

package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * A DTO that represents the AWS price list terms.
 *
 * @author matthi-g
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceTerms {

    @JsonAlias(value = "OnDemand")
    private Map<String, Map<String, AWSPriceTerm>> OnDemand;
}

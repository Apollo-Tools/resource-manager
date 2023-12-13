package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * A DTO that represents the AWS price list containing products and terms.
 *
 * @author matthi-g
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceList {

    private Map<String, AWSPriceProduct> products;

    private AWSPriceTerms terms;
}

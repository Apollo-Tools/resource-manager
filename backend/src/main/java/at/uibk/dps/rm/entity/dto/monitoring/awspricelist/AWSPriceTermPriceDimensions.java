package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceTermPriceDimensions {

    @JsonAlias(value = "beginRange")
    private String beginRange;

    @JsonAlias(value = "endRange")
    private String endRange;

    private String unit;

    @JsonAlias(value = "pricePerUnit")
    private AWSPricePricePerUnit pricePerUnit;
}

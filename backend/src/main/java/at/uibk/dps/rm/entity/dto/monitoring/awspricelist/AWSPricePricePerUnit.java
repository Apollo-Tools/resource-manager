package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPricePricePerUnit {
    @JsonAlias("USD")
    private BigDecimal usd;
}

package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceProductAttributes {

    private String group;

    @JsonAlias(value = "groupDescription")
    private String groupDescription;

    private String usagetype;

    private String servicename;
}

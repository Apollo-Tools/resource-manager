package at.uibk.dps.rm.entity.dto.monitoring.awspricelist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AWSPriceProductAttributes {

    // Lambda attributes
    private String group;

    @JsonAlias(value = "groupDescription")
    private String groupDescription;

    private String usagetype;

    private String servicename;

    // EC2 attributes
    @JsonAlias(value = "instanceType")
    private String instanceType;

    private String instancesku;

    private String tenancy;

    @JsonAlias(value = "operatingSystem")
    private String operatingSystem;

    @JsonAlias(value = "preInstalledSw")
    private String preInstalledSw;

    private String memory;
}

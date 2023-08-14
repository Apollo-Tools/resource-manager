package at.uibk.dps.rm.entity.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDTO {
    private String dbHost;
    private Integer dbPort;
    private String dbUser;
    private String dbPassword;
    private Integer apiPort;
    private String buildDirectory;
    private String dindDirectory;
    private String uploadPersistDirectory;
    private String uploadTempDirectory;
    private Long maxFileSize;
    private String jwtSecret;
    private String jwtAlgorithm;
    private Integer tokenMinutesValid;
    private Double ensembleValidationPeriod;
    private List<String> dockerInsecureRegistries;
    private String kubeConfigSecretsName;
    private String kubeConfigSecretsNamespace;
    private Integer kubeApiTimeoutSeconds;
    private Double kubeMonitoringPeriod;
    private List<String> kubeImagePullSecrets;
}

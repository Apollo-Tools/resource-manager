package at.uibk.dps.rm.entity.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDTO {
    private String dbHost;
    private Long dbPort;
    private String dbUser;
    private String dbPassword;
    private Long apiPort;
    private String buildDirectory;
    private String dindDirectory;
    private String uploadPersistDirectory;
    private String uploadTempDirectory;
    private Long maxFileSize;
    private String jwtSecret;
    private String jwtAlgorithm;
    private Long tokenMinutesValid;
    private Long ensembleValidationPeriod;
    private String kubeConfigSecretsName;
    private String kubeConfigSecretsNamespace;
}

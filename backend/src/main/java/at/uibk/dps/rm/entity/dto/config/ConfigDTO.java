package at.uibk.dps.rm.entity.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the vertx config.
 *
 * @author matthi-g
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDTO {
    private String dbHost;
    private Integer dbPort;
    private String dbUser;
    private String dbPassword;
    private int maxRetries;
    private int retryDelayMillis;
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
    private String kubeConfigDirectory;
    private Integer kubeApiTimeoutSeconds;
    private List<String> kubeImagePullSecrets;

    private String monitoringPushUrl;
    private String monitoringQueryUrl;
    private Double kubeMonitoringPeriod;
    private Double openfaasMonitoringPeriod;
    private Double regionMonitoringPeriod;
    private Double awsPriceMonitoringPeriod;
}

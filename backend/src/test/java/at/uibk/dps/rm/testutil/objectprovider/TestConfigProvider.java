package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;

import java.util.List;

/**
 * Utility class to instantiate vertx config objects.
 *
 * @author matthi-g
 */
public class TestConfigProvider {

    public static ConfigDTO getConfigDTO() {
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setDbHost("localhost");
        configDTO.setDbPort(5432);
        configDTO.setDbUser("root");
        configDTO.setDbPassword("root");
        configDTO.setApiPort(8888);
        configDTO.setBuildDirectory("build");
        configDTO.setDindDirectory("var/lib/apollo-rm/");
        configDTO.setUploadPersistDirectory("upload/persist");
        configDTO.setUploadTempDirectory("upload/temp");
        configDTO.setMaxFileSize(100_000_000L);
        configDTO.setJwtSecret("test-secret");
        configDTO.setJwtAlgorithm("test-HS256");
        configDTO.setTokenMinutesValid(1080);
        configDTO.setEnsembleValidationPeriod(60.0);
        configDTO.setDockerInsecureRegistries(List.of("localhost:5000"));
        configDTO.setKubeConfigSecretsName("kubesecrets");
        configDTO.setKubeConfigSecretsNamespace("default");
        configDTO.setKubeConfigDirectory("tmp/kubeconfig");
        configDTO.setKubeApiTimeoutSeconds(10);
        configDTO.setKubeImagePullSecrets(List.of("regcred"));

        configDTO.setKubeMonitoringPeriod(5.0);
        configDTO.setRegionMonitoringPeriod(5.0);
        return configDTO;
    }
}

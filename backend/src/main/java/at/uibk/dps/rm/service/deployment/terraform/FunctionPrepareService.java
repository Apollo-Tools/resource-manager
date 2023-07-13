package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageJavaCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

/**
 * This service prepares all functions for deployment. This includes composition and packaging of
 * the source code and composing the content of the OpenFaaS stack file.
 *
 * @author matthi-g
 */
public class FunctionPrepareService {

    private final Vertx vertx;

    private final FileSystem fileSystem;

    private final List<FunctionDeployment> functionDeployments;

    private final DeploymentPath deploymentPath;

    private final DockerCredentials dockerCredentials;

    private final Set<Long> functionIds = new HashSet<>();

    private FunctionsToDeploy functionsToDeploy = new FunctionsToDeploy();

    /**
     * Create an instance from vertx, functionDeployments, functionsDir and dockerCredentials.
     *
     * @param vertx the vertx instance
     * @param functionDeployments the list of function deployments
     * @param deploymentPath the deployment path of the module
     * @param dockerCredentials the credentials of the docker user
     */
    public FunctionPrepareService(Vertx vertx, List<FunctionDeployment> functionDeployments, DeploymentPath deploymentPath,
                               DockerCredentials dockerCredentials) {
        this.vertx = vertx;
        this.fileSystem = vertx.fileSystem();
        this.functionDeployments = functionDeployments;
        this.deploymentPath = deploymentPath;
        this.dockerCredentials = dockerCredentials;
    }

    /**
     * Package the source code for all function resources.
     *
     * @return a Single that emits the functions to deploy
     */
    public Single<FunctionsToDeploy> packageCode() {
        functionsToDeploy = new FunctionsToDeploy();
        PackageSourceCode packageSourceCode;
        StringBuilder functionsString = new StringBuilder();
        List<Completable> completables = new ArrayList<>();
        Set<RuntimeEnum> copiedOpenFaasTemplates = new HashSet<>();
        for (FunctionDeployment fr : functionDeployments) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String functionIdentifier =  function.getFunctionDeploymentId();
            RuntimeEnum runtime;
            try {
                runtime = RuntimeEnum.fromRuntime(function.getRuntime());
            } catch (RuntimeNotSupportedException ex) {
                return Single.error(ex);
            }
            if (runtime.equals(RuntimeEnum.PYTHON38)) {
                packageSourceCode = new PackagePythonCode(vertx, fileSystem, deploymentPath, function);
            } else if (runtime.equals(RuntimeEnum.JAVA11)) {
                packageSourceCode = new PackageJavaCode(vertx, fileSystem, deploymentPath, function);
            } else {
                return Single.error(RuntimeNotSupportedException::new);
            }
            completables.add(packageSourceCode.composeSourceCode());
            if (deployFunctionOnOpenFaaS(function)) {
                functionsString.append(getOpenFaasTemplateBlock(functionIdentifier, runtime));
                if (!copiedOpenFaasTemplates.contains(runtime)) {
                    completables.add(copyOpenFaasTemplate(runtime));
                    copiedOpenFaasTemplates.add(runtime);
                }
                functionsToDeploy.getDockerFunctionIdentifiers().add(functionIdentifier);
            }
            functionIds.add(function.getFunctionId());
            functionsToDeploy.getFunctionIdentifiers().add(functionIdentifier);
        }
        functionsToDeploy.setDockerFunctionsString(functionsString.toString());
        // TODO: add check if this is necessary (=no changes since last push)
        if (completables.isEmpty()) {
            return Single.just(functionsToDeploy);
        }

        return Completable.merge(completables)
            .andThen(Single.fromCallable(() -> functionsToDeploy));
    }

    /**
     * Check if a function is going to be deployed on a virtual machine or self-managed device.
     *
     * @param function the function
     * @return true if the function has to be deployed on a vm or edge device, else false
     */
    private boolean deployFunctionOnOpenFaaS(Function function) {
        return functionDeployments.stream().anyMatch(functionDeployment -> {
            PlatformEnum platform = PlatformEnum.fromPlatform(functionDeployment.getResource().getPlatform());
            return functionDeployment.getFunction().equals(function) &&
                (platform.equals(PlatformEnum.OPENFAAS) || platform.equals(PlatformEnum.EC2));
        });
    }

    private String getOpenFaasTemplateBlock(String functionIdentifier, RuntimeEnum runtimeEnum) {
        String functionPath = "./" + functionIdentifier +
            (runtimeEnum.equals(RuntimeEnum.JAVA11) ? "/function" : "");
        return String.format(
            "  %s:\n" +
                "    lang: %s-apollo-rm\n" +
                "    handler: %s\n" +
                "    image: %s/%s:latest\n",
            functionIdentifier, runtimeEnum.getDotlessValue(), functionPath, dockerCredentials.getUsername(),
            functionIdentifier);
    }

    private Completable copyOpenFaasTemplate(RuntimeEnum runtime) {
        String templatePath;
        String destinationPath;
        switch (runtime) {
            case PYTHON38:
                templatePath = Path
                    .of("faas-templates", runtime.getDotlessValue(), "openfaas")
                    .toAbsolutePath().toString();
                destinationPath = Path.of(deploymentPath.getTemplatesFolder().toString(),
                    runtime.getDotlessValue() + "-apollo-rm").toAbsolutePath().toString();
                return fileSystem.mkdirs(destinationPath)
                    .andThen(fileSystem.copyRecursive(templatePath, destinationPath, true));
            case JAVA11:
                templatePath = Path
                    .of("faas-templates", runtime.getDotlessValue())
                    .toAbsolutePath().toString();
                String wrapperPath = Path.of(templatePath, "wrapper").toAbsolutePath().toString();
                String entryPointPath = Path.of(templatePath, "openfaas").toAbsolutePath().toString();
                String modelPath = Path.of(templatePath, "apollorm", "model").toAbsolutePath().toString();
                destinationPath = Path.of(deploymentPath.getTemplatesFolder().toString(),
                    runtime.getDotlessValue() + "-apollo-rm").toAbsolutePath().toString();
                String destinationEntrypointPath =
                    Path.of(destinationPath, "entrypoint").toAbsolutePath().toString();
                String destinationModelPath =
                    Path.of(destinationPath, "model").toAbsolutePath().toString();
                return fileSystem.mkdirs(wrapperPath)
                    .andThen(fileSystem.mkdirs(destinationEntrypointPath))
                    .andThen(fileSystem.mkdirs(destinationModelPath))
                    .andThen(fileSystem.copyRecursive(wrapperPath, destinationPath, true))
                    .andThen(fileSystem.copyRecursive(entryPointPath, destinationEntrypointPath, true))
                    .andThen(fileSystem.copyRecursive(modelPath, destinationModelPath, true));
        }
        return Completable.error(RuntimeNotSupportedException::new);
    }
}

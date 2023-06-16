package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
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

    private final Path functionsDir;

    private final DockerCredentials dockerCredentials;

    private final Set<Long> functionIds = new HashSet<>();

    private FunctionsToDeploy functionsToDeploy = new FunctionsToDeploy();

    /**
     * Create an instance from vertx, functionDeployments, functionsDir and dockerCredentials.
     *
     * @param vertx the vertx instance
     * @param functionDeployments the list of function deployments
     * @param functionsDir the directory where everything related to the functions is stored
     * @param dockerCredentials the credentials of the docker user
     */
    public FunctionPrepareService(Vertx vertx, List<FunctionDeployment> functionDeployments, Path functionsDir,
                               DockerCredentials dockerCredentials) {
        this.vertx = vertx;
        this.fileSystem = vertx.fileSystem();
        this.functionDeployments = functionDeployments;
        this.functionsDir = functionsDir;
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
        Set<Runtime> copiedOpenFaasTemplates = new HashSet<>();
        for (FunctionDeployment fr : functionDeployments) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String functionIdentifier =  function.getFunctionDeploymentId();
            if (function.getRuntime().getName().startsWith("python")) {
                packageSourceCode = new PackagePythonCode(vertx, fileSystem, function);
                completables.add(packageSourceCode.composeSourceCode(functionsDir));
                if (deployFunctionOnOpenFaaS(function)) {
                    functionsString.append(String.format(
                        "  %s:\n" +
                            "    lang: python3-apollo-rm\n" +
                            "    handler: ./%s\n" +
                            "    image: %s/%s:latest\n",
                        functionIdentifier, functionIdentifier, dockerCredentials.getUsername(), functionIdentifier));
                    if (!copiedOpenFaasTemplates.contains(function.getRuntime())) {
                        completables.add(copyOpenFaasTemplates(function.getRuntime()));
                        copiedOpenFaasTemplates.add(function.getRuntime());
                    }
                    functionsToDeploy.getDockerFunctionIdentifiers().add(functionIdentifier);
                }
            } else {
                return Single.error(RuntimeNotSupportedException::new);
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
            PlatformEnum platform = PlatformEnum.fromString(
                functionDeployment.getResource().getPlatform().getPlatform());
            return functionDeployment.getFunction().equals(function) &&
                (platform.equals(PlatformEnum.OPENFAAS) || platform.equals(PlatformEnum.EC2));
        });
    }

    private Completable copyOpenFaasTemplates(Runtime runtime) {
        String templatePath = Path
            .of("faas-templates", runtime.getName().replace(".", ""), "openfaas")
            .toAbsolutePath().toString();
        String destinationPath = Path
            .of(functionsDir.toString(), "template", "python3-apollo-rm")
            .toAbsolutePath().toString();
        return fileSystem.mkdirs(destinationPath)
            .andThen(fileSystem.copyRecursive(templatePath, destinationPath, true));
    }
}

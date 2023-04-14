package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

public class FunctionFileService {

    private final Vertx vertx;

    private final FileSystem fileSystem;

    private final List<FunctionResource> functionResources;

    private final Path functionsDir;

    private final DockerCredentials dockerCredentials;

    private final Set<Long> functionIds = new HashSet<>();

    private FunctionsToDeploy functionsToDeploy = new FunctionsToDeploy();

    public FunctionFileService(Vertx vertx, List<FunctionResource> functionResources, Path functionsDir,
                               DockerCredentials dockerCredentials) {
        this.vertx = vertx;
        this.fileSystem = vertx.fileSystem();
        this.functionResources = functionResources;
        this.functionsDir = functionsDir;
        this.dockerCredentials = dockerCredentials;
    }

    public Single<FunctionsToDeploy> packageCode() {
        functionsToDeploy = new FunctionsToDeploy();
        PackageSourceCode packageSourceCode;
        StringBuilder functionsString = functionsToDeploy.getFunctionsString();
        List<Completable> completables = new ArrayList<>();
        for (FunctionResource fr : functionResources) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String functionIdentifier =  function.getFunctionDeploymentId();
            if (function.getRuntime().getName().startsWith("python")) {
                packageSourceCode = new PackagePythonCode(vertx, fileSystem);
                completables.add(packageSourceCode.composeSourceCode(functionsDir, functionIdentifier,
                    function.getCode()));
                if (deployFunctionOnVMOrEdge(function, functionResources)) {
                    functionsString.append(String.format(
                        "  %s:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./%s\n" +
                            "    image: %s/%s:latest\n", functionIdentifier, functionIdentifier,
                        dockerCredentials.getUsername(), functionIdentifier));
                }
            } else {
                return Single.error(RuntimeNotSupportedException::new);
            }
            functionIds.add(function.getFunctionId());
            functionsToDeploy.getFunctionIdentifiers().add(functionIdentifier);
        }
        // TODO: add check if this is necessary (=no changes since last push)
        if (completables.isEmpty()) {
            return Single.just(functionsToDeploy);
        }

        return Completable.merge(completables)
            .andThen(Single.fromCallable(() -> functionsToDeploy));
    }

    private boolean deployFunctionOnVMOrEdge(Function function, List<FunctionResource> functionResources) {
        return functionResources.stream().anyMatch(functionResource -> {
            String resourceType = functionResource.getResource().getResourceType().getResourceType();
            return functionResource.getFunction().equals(function) &&
                (resourceType.equals("edge") || resourceType.equals("vm"));
        });
    }
}

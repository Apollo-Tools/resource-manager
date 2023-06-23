package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.Map;

/**
 * Extends the #PackageSourceCode class for source code that is written in java.
 *
 * @author matthi-g
 */
public class PackageJavaCode extends PackageSourceCode {

    private final Map<Path, String> HANDLER_FILES = Map
        .of(Path.of("faas-templates", "java11", "apollorm", "model"), "model",
            Path.of("faas-templates", "java11", "lambda"), "entrypoint",
            Path.of("faas-templates", "java11", "wrapper", "build.gradle"), "build.gradle",
            Path.of("faas-templates", "java11", "wrapper", "settings.gradle"), "settings.gradle");

    private final Function function;

    private final Vertx vertx;

    private final DeploymentPath deploymentPath;

    /**
     * Create an instance from vertx, the fileSystem and a function.
     *
     * @param vertx a vertx instance
     * @param fileSystem the vertx file system
     * @param deploymentPath the deployment path of the module
     * @param function the function
     */
    public PackageJavaCode(Vertx vertx, FileSystem fileSystem, DeploymentPath deploymentPath, Function function) {
        super(vertx, fileSystem, deploymentPath.getFunctionsFolder(), function, "function/src");
        this.function = function;
        this.vertx = vertx;
        this.deploymentPath = deploymentPath;
    }

    protected Completable createSourceCode() {
        if (function.getIsFile()) {
            return new ConfigUtility(vertx).getConfig().flatMapCompletable(config -> {
                Path zipPath = Path.of(config.getString("upload_persist_directory"), function.getCode());
                return vertx.executeBlocking(fut -> {
                    unzipAllFiles(zipPath, Path.of(deploymentPath.getFunctionsFolder().toString(), "function"));
                    fut.complete();
                }).ignoreElement();
            })
                .andThen(Observable.fromIterable(HANDLER_FILES.entrySet()))
                .flatMapCompletable(entry -> vertx.fileSystem().copyRecursive(entry.getKey().toString(),
                    Path.of(deploymentPath.getFunctionsFolder().toString(), function.getFunctionDeploymentId(),
                        entry.getValue()).toString(), true));
        } else {
            return Completable.error(new RuntimeException("runtime only supports zip deployments"));
        }
    }

    @Override
    protected void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) {
        // TODO: remove from abstract class
    }
}

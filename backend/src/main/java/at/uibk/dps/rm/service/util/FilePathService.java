package at.uibk.dps.rm.service.util;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for file path operations.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FilePathService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FilePathService create(Vertx vertx) {
        return new FilePathServiceImpl(vertx);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FilePathService createProxy(Vertx vertx) {
        return new FilePathServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress("filepath"));
    }

    /**
     * Check whether a file exists at the given templatePath.
     *
     * @param templatePath the path to the template
     * @return a Future that emits true if the file exists, else false
     */
    Future<Boolean> templatePathExists(String templatePath);

    /**
     * Get the file content of a template file.
     *
     * @param templatePath the path to the template file
     * @return a Future that emits the file content
     */
    Future<String> getRuntimeTemplate(String templatePath);

    /**
     * Check whether the terraform lock file exists at the tfPath
     * @param tfPath the path to the directory where the lock should be located
     * @return a Future that emits true if the lock file exists, else false
     */
    Future<Boolean> tfLocFileExists(String tfPath);
}

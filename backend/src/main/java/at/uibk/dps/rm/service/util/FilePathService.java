package at.uibk.dps.rm.service.util;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface FilePathService extends ServiceInterface {

    @Generated
    @GenIgnore
    static FilePathService create(Vertx vertx) {
        return new FilePathServiceImpl(vertx);
    }

    @Generated
    static FilePathService createProxy(Vertx vertx) {
        return new FilePathServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress("filepath"));
    }

    Future<Boolean> templatePathExists(String templatePath);

    Future<String> getRuntimeTemplate(String templatePath);

    Future<Boolean> tfLocFileExists(String tfPath);
}

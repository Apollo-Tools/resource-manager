package at.uibk.dps.rm.service.util;

import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.nio.file.Path;

public class FilePathServiceImpl extends ServiceProxy implements FilePathService{

    private final Vertx vertx;


    public FilePathServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public String getServiceProxyAddress() {
        return "filepath" + super.getServiceProxyAddress();
    }

    @Override
    public Future<Boolean> templatePathExists(String templatePath) {
        return vertx.fileSystem().exists(templatePath)
            .map(result -> result || templatePath.equals(""));
    }

    @Override
    public Future<Boolean> tfLocFileExists(String tfPath) {
        Path lockFilePath = Path.of(tfPath, ".terraform.lock.hcl");
        return vertx.fileSystem().exists(lockFilePath.toString());
    }

    @Override
    public Future<String> getRuntimeTemplate(String templatePath) {
        return vertx.fileSystem().readFile(templatePath)
            .map(buffer -> buffer.getString(0, buffer.length()));
    }
}

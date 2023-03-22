package at.uibk.dps.rm.service.util;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class FilePathServiceImpl implements FilePathService{

    private final Vertx vertx;


    public FilePathServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<Boolean> templatePathExists(String templatePath) {
        return vertx.fileSystem().exists(templatePath)
            .map(result -> result || templatePath.equals(""));
    }

    @Override
    public Future<String> getRuntimeTemplate(String templatePath) {
        return vertx.fileSystem().readFile(templatePath)
            .map(buffer -> buffer.getString(0, buffer.length() - 1));
    }
}

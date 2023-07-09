package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;

@UtilityClass
public class UploadFileHelper {

    public static Completable persistUploadedFile(Vertx vertx, String fileName) {
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                Path tempPath = Path.of(config.getString("upload_temp_directory"), fileName);
                Path destPath = Path.of(config.getString("upload_persist_directory"), fileName);
                return vertx.fileSystem().copy(tempPath.toString(), destPath.toString());
            });
    }

    public static Completable deleteFile(Vertx vertx, String fileName) {
        return new ConfigUtility(vertx).getConfig().flatMapCompletable(config -> {
            Path filePath = Path.of(config.getString("upload_persist_directory"), fileName);
            return vertx.fileSystem()
                .exists(filePath.toString())
                .flatMapCompletable(exists -> {
                    if (exists) {
                        return vertx.fileSystem().delete(filePath.toString());
                    } else {
                        return Completable.complete();
                    }
                });
        });
    }

    public static Completable updateFile(Vertx vertx, String oldFileName, String newFileName) {
        return deleteFile(vertx, oldFileName)
            .andThen(persistUploadedFile(vertx, newFileName));
    }

}

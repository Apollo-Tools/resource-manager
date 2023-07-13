package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;

/**
 * This class is used to persist, delete or update persisted file uploads.
 *
 * @author matthi-g
 */
@UtilityClass
public class UploadFileHelper {

    /**
     * Move an uploaded file from the temp directory to the persist directory.
     *
     * @param vertx the vertx instance
     * @param fileName the name of the file
     * @return a Completable
     */
    public static Completable persistUploadedFile(Vertx vertx, String fileName) {
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                Path tempPath = Path.of(config.getString("upload_temp_directory"), fileName);
                Path destPath = Path.of(config.getString("upload_persist_directory"), fileName);
                return vertx.fileSystem().copy(tempPath.toString(), destPath.toString());
            });
    }

    /**
     * Delete a persisted file.
     *
     * @param vertx the vertx instance
     * @param fileName the name of the file
     * @return a Completable
     */
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

    /**
     * Delete the old file and replace it with the new one.
     *
     * @param vertx the vertx instance
     * @param oldFileName the name of the old file
     * @param newFileName the name of the new file
     * @return a Completable
     */
    public static Completable updateFile(Vertx vertx, String oldFileName, String newFileName) {
        return deleteFile(vertx, oldFileName)
            .andThen(persistUploadedFile(vertx, newFileName));
    }

}

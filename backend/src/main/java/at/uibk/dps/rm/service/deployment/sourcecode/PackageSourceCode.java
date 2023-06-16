package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.model.Function;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This abstract class defines methods to compose and package the source code of functions for
 * deployment.
 *
 * @author matthi-g
 */
public abstract class PackageSourceCode {

    private final Vertx vertx;

    private final FileSystem fileSystem;

    private final String SOURCE_CODE_NAME = "cloud_function.txt";

    private String sourceCodeName;

    private final Function function;

    /**
     * Create an instance from vertx and the fileSystem.
     *
     * @param vertx a vertx instance
     * @param fileSystem the vertx file system
     */
    public PackageSourceCode(Vertx vertx, FileSystem fileSystem, Function function) {
        this.vertx = vertx;
        this.fileSystem = fileSystem;
        this.sourceCodeName = SOURCE_CODE_NAME;
        this.function = function;
    }

    protected String getSourceCodeName() {
        return sourceCodeName;
    }

    protected void setSourceCodeName(String sourceCodeName) {
        this.sourceCodeName = sourceCodeName;
    }

    /**
     * Compose the source code of a function for upcoming deployment.
     * <a href="https://www.baeldung.com/java-compress-and-uncompress">source</a>
     *
     * @param rootFolder the folder where the source code should be created
     * @return a Completable
     */
    public Completable composeSourceCode(Path rootFolder) {
        return createSourceCodeFiles(rootFolder, function.getFunctionDeploymentId(), sourceCodeName)
            .flatMapMaybe(sourceCodePath -> vertx.executeBlocking(fut -> {
                    zipAllFiles(rootFolder, sourceCodePath, function.getFunctionDeploymentId());
                    fut.complete();
                }))
            .ignoreElement();
    }

    /**
     * Create the source files of a function to deploy.
     *
     * @param rootFolder the folder where the source code should be created
     * @param functionIdentifier the identifier of the function
     * @param fileName the file name
     * @return a Single that emits the path to the source code file
     */
    private Single<Path> createSourceCodeFiles(Path rootFolder, String functionIdentifier, String fileName) {
        Path sourceCodePath = Path.of(rootFolder.toString(), functionIdentifier, fileName);
        return fileSystem.mkdirs(sourceCodePath.getParent().toString())
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMapCompletable(res -> createSourceCodeFiles(sourceCodePath))
            .toSingle(() -> sourceCodePath);
    }

    private Completable createSourceCodeFiles(Path sourceCodePath) {
        if (function.getIsFile()) {
            return vertx.executeBlocking(fut -> {
                String zipPath = function.getCode();
                unzipAllFiles(Path.of(zipPath), sourceCodePath.getParent(), function.getFunctionDeploymentId());
                fut.complete();
            }).ignoreElement();
        } else {
            return fileSystem.writeFile(sourceCodePath.toString(), Buffer.buffer(function.getCode()));
        }
    }

    /**
     * Zip all files necessary for the function deployment.
     *
     * @param rootFolder the folder for all files
     * @param sourceCode the path to the source code
     * @param functionIdentifier the identifier of the function
     */
    protected abstract void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier);

    /**
     * Write the fileToZip to the zipOutputStream
     *
     * @param fileToZip the file to zip
     * @param zipOutputStream the zip output stream
     * @throws IOException if an I/O Exception occurs
     */
    protected void zipFile(File fileToZip, ZipOutputStream zipOutputStream) throws IOException {
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOutputStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
        }
        fis.close();
    }

    protected abstract void unzipAllFiles(Path filePath, Path detinationPath, String functionIdentifier);
}

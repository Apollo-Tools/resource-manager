package at.uibk.dps.rm.service.deployment.sourcecode;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class PackageSourceCode {

    private final Vertx vertx;

    private final FileSystem fileSystem;

    private final String SOURCE_CODE_NAME = "cloud_function.txt";

    private String sourceCodeName;

    public PackageSourceCode(Vertx vertx, FileSystem fileSystem) {
        this.vertx = vertx;
        this.fileSystem = fileSystem;
        this.sourceCodeName = SOURCE_CODE_NAME;
    }

    protected String getSourceCodeName() {
        return sourceCodeName;
    }

    protected void setSourceCodeName(String sourceCodeName) {
        this.sourceCodeName = sourceCodeName;
    }

    // Src: https://www.baeldung.com/java-compress-and-uncompress
    public Completable composeSourceCode(Path rootFolder, String functionIdentifier, String code) {
        return createSourceCodeFile(rootFolder, functionIdentifier, code, sourceCodeName)
            .flatMapMaybe(sourceCodePath -> vertx.executeBlocking(fut -> {
                    zipAllFiles(rootFolder, sourceCodePath, functionIdentifier);
                    fut.complete();
                }))
            .ignoreElement();
    }

    protected Single<Path> createSourceCodeFile(Path root, String functionIdentifier, String code, String fileName) {
        Path sourceCodePath = Path.of(root.toString(), functionIdentifier, fileName);
        return fileSystem.mkdirs(sourceCodePath.getParent().toString())
                .andThen(fileSystem.writeFile(sourceCodePath.toString(), Buffer.buffer(code)))
                .toSingle(() -> sourceCodePath);
    }

    protected abstract void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier);

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
}

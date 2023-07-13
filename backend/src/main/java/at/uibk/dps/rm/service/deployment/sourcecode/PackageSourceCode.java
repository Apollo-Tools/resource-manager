package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

    private final Function function;

    private final Path rootFolder;

    private final Path sourceCodePath;


    /**
     * Create an instance from vertx and the fileSystem.
     *
     * @param vertx a vertx instance
     * @param fileSystem the vertx file system
     */
    public PackageSourceCode(Vertx vertx, FileSystem fileSystem, Path rootFolder, Function function,
            String sourceCodeName) {
        this.vertx = vertx;
        this.fileSystem = fileSystem;
        this.rootFolder = rootFolder;
        this.function = function;
        this.sourceCodePath = Path.of(rootFolder.toString(), function.getFunctionDeploymentId(), sourceCodeName);
    }

    /**
     * Compose the source code of a function for upcoming deployment.
     * <a href="https://www.baeldung.com/java-compress-and-uncompress">source</a>
     *
     * @return a Completable
     */
    public Completable composeSourceCode() {
        return createSourceCodeFiles()
            .flatMapMaybe(sourceCodePath -> vertx.executeBlocking(fut -> {
                    zipAllFiles(rootFolder, sourceCodePath, function.getFunctionDeploymentId());
                    fut.complete();
                }))
            .ignoreElement();
    }

    /**
     * Create the source files of a function to deploy.
     *
     * @return a Single that emits the path to the source code file
     */
    private Single<Path> createSourceCodeFiles() {
        return fileSystem.mkdirs(sourceCodePath.getParent().toString())
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMapCompletable(res -> createSourceCode())
            .toSingle(() -> sourceCodePath);
    }

    /**
     * Either unzip the code of a function if it was uploaded as zip file or create a file for
     * the manually created code.
     *
     * @return a Completable
     */
    protected Completable createSourceCode() {
        if (function.getIsFile()) {
            return new ConfigUtility(vertx).getConfig().flatMapCompletable(config -> {
                Path zipPath = Path.of(config.getString("upload_persist_directory"), function.getCode());
                return vertx.executeBlocking(fut -> {
                    unzipAllFiles(zipPath);
                    fut.complete();
                }).ignoreElement();
            });
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
    protected void zipFile(File fileToZip, String fileName, ZipOutputStream zipOutputStream) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        ZipEntry zipEntry = new ZipEntry(fileName);
        if (fileToZip.isDirectory()) {
            if (!fileToZip.getName().endsWith("/")) {
                zipEntry = new ZipEntry(fileName + "/");
            }
            zipOutputStream.putNextEntry(zipEntry);
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOutputStream);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        zipOutputStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * Unzip all files that are contained in filePath.
     *
     * @param filePath the path to the zip file
     */
    protected void unzipAllFiles(Path filePath) {
        File destDir = sourceCodePath.getParent().toFile();
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath.toString()));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                List<File> newFiles = getUnzippedFileDest(zipEntry, destDir);
                readAndSaveUnzippedFile(zipEntry, newFiles, zis);
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the list of destination for a zipEntry.
     *
     * @param zipEntry the zip entry
     * @param destDir the main destination directory
     * @return a List of files that point to the destinations of the zip entry
     * @throws IOException if the destDir is invalid
     */
    protected List<File> getUnzippedFileDest(ZipEntry zipEntry, File destDir) throws IOException {
        List<File> newFiles = new ArrayList<>();
        newFiles.add(newFile(destDir, zipEntry));
        return newFiles;
    }

    /**
     * Decompress a zip entry and save it to the destination paths.
     *
     * @param zipEntry the zip entry
     * @param newFiles the destination paths
     * @param zis the zip input stream
     * @throws IOException if a bad destination directory is passed to the method
     */
    private void readAndSaveUnzippedFile(ZipEntry zipEntry, List<File> newFiles, ZipInputStream zis) throws IOException {
        byte[] buffer = new byte[1024];
        if (zipEntry.isDirectory()) {
            for (File file : newFiles) {
                if (!file.isDirectory() && !file.mkdirs() && !file.exists()) {
                    throw new IOException("Failed to create directory " + file);
                }
            }
        } else {
            List<FileOutputStream> fos = new ArrayList<>();
            for (File file : newFiles) {
                // fix for Windows-created archives
                File parent = file.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs() && !parent.exists()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                fos.add(new FileOutputStream(file));
            }
            // write file content
            int len;
            while ((len = zis.read(buffer)) > 0) {
                for (FileOutputStream stream : fos) {
                    stream.write(buffer, 0, len);
                }
            }
            for (FileOutputStream stream : fos) {
                stream.close();
            }
        }
    }

    /**
     * Create a new file from a destination directory and zip entry.
     *
     * @param destinationDir the destination directore
     * @param zipEntry the zip entry
     * @return the newly file
     * @throws IOException if a bad destination directory is passed to the method
     */
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}

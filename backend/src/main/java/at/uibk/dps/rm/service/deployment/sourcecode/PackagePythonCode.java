package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.model.Function;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Extends the #PackageSourceCode class for source code that is written in python.
 *
 * @author matthi-g
 */
public class PackagePythonCode extends PackageSourceCode{
    protected final String SOURCE_CODE_NAME = "main.py";
    private final File[] HANDLER_FILES =
        {Path.of("faas-templates", "python38", "lambda", "lambda.py").toFile()};

    /**
     * Create an instance from vertx, the fileSystem and a function.
     *
     * @param vertx a vertx instance
     * @param fileSystem the vertx file system
     * @param function the function
     */
    public PackagePythonCode(Vertx vertx, FileSystem fileSystem, Function function) {
        super(vertx, fileSystem, function);
        setSourceCodeName(SOURCE_CODE_NAME);
    }

    @Override
    protected void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(rootFolder + "/" + functionIdentifier + ".zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            List<File> filesToZip = new ArrayList<>();
            filesToZip.add(sourceCode.toFile());
            filesToZip.addAll(Arrays.asList(HANDLER_FILES));
            for (File fileToZip : filesToZip) {
                zipFile(fileToZip, zipOutputStream);
            }
            zipOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void unzipAllFiles(Path filePath, Path detinationPath, String functionIdentifier) {
        File destDir = detinationPath.toFile();
        byte[] buffer = new byte[1024];
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath.toString()));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs() && !newFile.exists()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs() && !newFile.exists()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

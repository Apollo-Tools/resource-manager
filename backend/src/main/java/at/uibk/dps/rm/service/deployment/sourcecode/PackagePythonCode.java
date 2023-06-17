package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.model.Function;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Extends the #PackageSourceCode class for source code that is written in python.
 *
 * @author matthi-g
 */
public class PackagePythonCode extends PackageSourceCode{

    private final FileSystem fileSystem;

    private final File[] HANDLER_FILES =
        {Path.of("faas-templates", "python38", "lambda", "lambda.py").toFile()};

    private final Path layerFolder;

    /**
     * Create an instance from vertx, the fileSystem and a function.
     *
     * @param vertx a vertx instance
     * @param fileSystem the vertx file system
     * @param deploymentPath the deployment path of the module
     * @param function the function
     */
    public PackagePythonCode(Vertx vertx, FileSystem fileSystem, DeploymentPath deploymentPath, Function function) {
        super(vertx, fileSystem, deploymentPath.getFunctionsFolder(), function, "main.py");
        this.fileSystem = fileSystem;
        this.layerFolder = Path.of(deploymentPath.getLayersFolder().toString(), function.getFunctionDeploymentId());
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
    protected List<File> getUnzippedFileDest(ZipEntry zipEntry, File destDir) throws IOException {
        List<File> newFiles = super.getUnzippedFileDest(zipEntry, destDir);
        if (zipEntry.getName().equals("requirements.txt") && zipEntry.getSize() > 0) {
            File layerDestDir = layerFolder.toFile();
            newFiles.add(newFile(layerDestDir, zipEntry));
        }
        fileSystem.mkdirsBlocking(layerFolder.toString());
        return newFiles;
    }
}

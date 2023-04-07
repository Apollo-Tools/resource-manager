package at.uibk.dps.rm.service.deployment.sourcecode;

import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class PackagePythonCode extends PackageSourceCode{
    protected final String SOURCE_CODE_NAME = "cloud_function.py";
    private final File[] HANDLER_FILES =
        {Path.of("src", "main", "resources", "faas", "python", "main.py").toFile(),
            Path.of("src", "main", "resources", "faas", "python", "handler.py").toFile()};

    public PackagePythonCode(Vertx vertx, FileSystem fileSystem) {
        super(vertx, fileSystem);
        setSourceCodeName(SOURCE_CODE_NAME);
    }

    @Override
    protected void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(rootFolder + "\\" + functionIdentifier + ".zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            List<File> filesToZip = new ArrayList<>();
            filesToZip.add(sourceCode.toFile());
            for (File file : HANDLER_FILES) {
                Path destinationFile = Path.of(rootFolder.toString(), functionIdentifier, file.getName());
                Files.copy(file.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                filesToZip.add(destinationFile.toFile());
            }

            for (File fileToZip : filesToZip) {
                zipFile(fileToZip, zipOutputStream);
            }
            zipOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

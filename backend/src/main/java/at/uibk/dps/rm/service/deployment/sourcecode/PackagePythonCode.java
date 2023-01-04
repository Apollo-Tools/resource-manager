package at.uibk.dps.rm.service.deployment.sourcecode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

public class PackagePythonCode extends PackageSourceCode{
    protected final String SOURCE_CODE_NAME = "cloud_function.py";
    private final File HANDLER_FILE = new File("backend\\src\\main\\resources\\faas\\python\\main.py");

    public PackagePythonCode() {
        setSourceCodeName(SOURCE_CODE_NAME);
    }

    @Override
    protected void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(rootFolder + "\\" + functionIdentifier + ".zip");
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        File mainFile = new File(String.valueOf(sourceCode));
        File[] filesToZip = {HANDLER_FILE, mainFile};
        for (File fileToZip : filesToZip) {
            zipFile(fileToZip, zipOutputStream);
        }
        zipOutputStream.close();
        fileOutputStream.close();
    }
}

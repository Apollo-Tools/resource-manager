package at.uibk.dps.rm.service.deployment.sourcecode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class PackageSourceCode {
    private final String SOURCE_CODE_NAME = "cloud_function.txt";

    private String sourceCodeName;

    public PackageSourceCode() {
        this.sourceCodeName = SOURCE_CODE_NAME;
    }

    protected String getSourceCodeName() {
        return sourceCodeName;
    }

    protected void setSourceCodeName(String sourceCodeName) {
        this.sourceCodeName = sourceCodeName;
    }

    // Src: https://www.baeldung.com/java-compress-and-uncompress
    public void composeSourceCode(Path rootFolder, String functionIdentifier, String code) throws IOException {
        Path sourceCode = createSourceCodeFile(rootFolder, functionIdentifier, code, sourceCodeName);
        zipAllFiles(rootFolder, sourceCode, functionIdentifier);
    }

    protected Path createSourceCodeFile(Path root, String functionIdentifier, String code, String fileName) throws IOException {
        Path sourceCode = Path.of(root.toString(), functionIdentifier, fileName);
        Files.createDirectories(sourceCode.getParent());
        Files.writeString(sourceCode, code, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        return sourceCode;
    }

    protected abstract void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) throws IOException;

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

package at.uibk.dps.rm.service.deployment.terraform;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@AllArgsConstructor
public abstract class TerraformFileService {

    private Path rootFolder;

    /*** main.tf ***/
    protected abstract String getProviderString();

    protected abstract String getMainFileContent() throws IOException;

    /*** variables.tf ***/
    protected abstract String getCredentialVariablesString();

    protected abstract String getVariablesFileContent();

    /*** outputs.tf ***/
    protected abstract String getOutputString();

    protected abstract String getOutputsFileContent();


    /*** setup directory ***/
    public void setUpDirectory() throws IOException {
        Files.createDirectories(rootFolder);

        String mainContent = this.getMainFileContent();
        createTerraformFile("main.tf", mainContent);

        String variableContent = this.getVariablesFileContent();
        createTerraformFile("variables.tf", variableContent);

        String outputContent = this.getOutputsFileContent();
        createTerraformFile("outputs.tf", outputContent);
    }

    private void createTerraformFile(String fileName, String fileContent) throws IOException {
        Path filePath = Paths.get(rootFolder + "\\" + fileName);
        Files.writeString(filePath, fileContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    /*** Getter ***/
    protected Path getRootFolder() {
        return this.rootFolder;
    }
}

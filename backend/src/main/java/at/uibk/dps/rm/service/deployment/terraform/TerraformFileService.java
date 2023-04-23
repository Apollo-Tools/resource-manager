package at.uibk.dps.rm.service.deployment.terraform;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import lombok.AllArgsConstructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class is used to setup the structure of terraform modules.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public abstract class TerraformFileService {

    private FileSystem fileSystem;

    private Path rootFolder;

    //*** main.tf ***//
    /**
     * Get the string that defines the terraform providers in the terraform module.
     *
     * @return the provider string
     */
    protected abstract String getProviderString();

    /**
     * Get the content of the main file of the terraform module.
     *
     * @return the content of the main file
     */
    protected abstract String getMainFileContent();

    //*** variables.tf ***//
    /**
     * Get the string that defines the credentials variables used in the terraform module.
     *
     * @return the credential variables string
     */
    protected abstract String getCredentialVariablesString();

    /**
     * Get the content of the variables file of the terraform module.
     *
     * @return the content of the variables file
     */
    protected abstract String getVariablesFileContent();

    //*** outputs.tf ***//
    /**
     * Get the string that defines output variables in the terraform module.
     *
     * @return the output variables string
     */
    protected abstract String getOutputString();

    /**
     * Get the file content of the outputs file of the terraform module.
     *
     * @return the conent of the outputs file
     */
    protected abstract String getOutputsFileContent();


    //*** setup directory ***//
    /**
     * Set up the terraform module directory.
     *
     * @return a Completable
     */
    public Completable setUpDirectory() {
        return fileSystem.mkdirs(rootFolder.toString())
            .andThen(createTerraformFiles());
    }

    /**
     * Create all terraform files for the terraform module.
     *
     * @return a Completable
     */
    private Completable createTerraformFiles() {
        List<Completable> completables = new ArrayList<>();

        String mainContent = this.getMainFileContent();
        completables.add(createTerraformFile("main.tf", mainContent));

        String variableContent = this.getVariablesFileContent();
        completables.add(createTerraformFile("variables.tf", variableContent));

        String outputContent = this.getOutputsFileContent();
        completables.add(createTerraformFile("outputs.tf", outputContent));
        return Completable.merge(completables);
    }

    /**
     * Creat a terraform file with fileName as name and fileContent as content in the root folder.
     *
     * @param fileName the file name
     * @param fileContent the content of the file
     * @return a Completable
     */
    private Completable createTerraformFile(String fileName, String fileContent) {
        Path filePath = Path.of(rootFolder.toString(), fileName);
        return fileSystem.writeFile(filePath.toString(), Buffer.buffer(fileContent));
    }

    /**
     * Delete all files and directories in the rootFolder.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the terraform module
     * @return a Completable
     */
    public static Completable deleteAllDirs(FileSystem fileSystem, Path rootFolder) {
        return fileSystem.deleteRecursive(rootFolder.toString(), true);
    }
}

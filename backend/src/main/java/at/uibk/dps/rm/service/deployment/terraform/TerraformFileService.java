package at.uibk.dps.rm.service.deployment.terraform;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import lombok.AllArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class TerraformFileService {

    private FileSystem fileSystem;

    private Path rootFolder;

    /*** main.tf ***/
    protected abstract String getProviderString();

    protected abstract String getMainFileContent();

    /*** variables.tf ***/
    protected abstract String getCredentialVariablesString();

    protected abstract String getVariablesFileContent();

    /*** outputs.tf ***/
    protected abstract String getOutputString();

    protected abstract String getOutputsFileContent();


    /*** setup directory ***/
    public Completable setUpDirectory() {
        return fileSystem.mkdirs(rootFolder.toString())
            .andThen(createTerraformFiles());
    }

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

    private Completable createTerraformFile(String fileName, String fileContent) {
        Path filePath = Paths.get(rootFolder + "\\" + fileName);
        return fileSystem.writeFile(filePath.toString(), Buffer.buffer(fileContent));
    }

    public static Completable deleteAllDirs(FileSystem fileSystem, Path rootFolder) {
        return fileSystem.deleteRecursive(rootFolder.toString(), true);
    }
}

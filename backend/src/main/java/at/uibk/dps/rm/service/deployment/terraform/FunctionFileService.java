package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionFileService {

    private final List<FunctionResource> functionResources;

    private final Path functionsDir;

    private final long reservationId;

    private final Set<Long> functionIds = new HashSet<>();

    public FunctionFileService(List<FunctionResource> functionResources, Path functionsDir, long reservationId) {
        this.functionResources = functionResources;
        this.reservationId = reservationId;
        this.functionsDir = functionsDir;
    }

    public void packageCode() throws IOException {
        PackageSourceCode packageSourceCode;
        StringBuilder functionsString = new StringBuilder();
        for (FunctionResource fr : functionResources) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
            if (runtime.startsWith("python")) {
                packageSourceCode = new PackagePythonCode();
                packageSourceCode.composeSourceCode(functionsDir, functionIdentifier,
                    function.getCode());

                // TODO: replace user name with environment variable
                functionsString.append(String.format(
                    "  %s:\n" +
                    "    lang: python3-flask-debian\n" +
                    "    handler: ./%s\n" +
                    "    image: %s/%s:latest\n", functionIdentifier, functionIdentifier, "matthigas", functionIdentifier));
            }
            functionIds.add(function.getFunctionId());
        }

        String stackFile = String.format(
            "version: 1.0\n" +
                "provider:\n" +
                "  name: openfaas\n" +
                "configuration:\n" +
                "  templates:\n" +
                "    - name: python3-flask-debian\n" +
                "functions:\n" +
                "%s\n", functionsString);
        createStackFile(functionsDir, "stack.yml", stackFile);
    }

    private void createStackFile(Path rootFolder, String fileName, String fileContent) throws IOException {
        Path filePath = Path.of(rootFolder.toString(), fileName);
        Files.writeString(filePath, fileContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }
}

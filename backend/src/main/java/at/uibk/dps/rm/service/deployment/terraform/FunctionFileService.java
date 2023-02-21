package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;

import java.io.IOException;
import java.nio.file.Path;
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
        for (FunctionResource fr : functionResources) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "") +
                "_" + reservationId;
            packageSourceCode = new PackagePythonCode();
            packageSourceCode.composeSourceCode(functionsDir, functionIdentifier,
                function.getCode());
        }
    }
}

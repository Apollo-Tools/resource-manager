package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import io.reactivex.rxjava3.core.Single;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface DockerImageChecker {
    Single<Set<Function>> getNecessaryFunctionBuilds(List<FunctionDeployment> functionDeployments);
    Single<Boolean> isUpToDate(String imageName, String tag, Date lastFunctionUpdate);
}

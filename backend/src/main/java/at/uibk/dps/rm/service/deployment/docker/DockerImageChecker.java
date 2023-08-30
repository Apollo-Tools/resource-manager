package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import io.reactivex.rxjava3.core.Single;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Interface to check if functions of a function deployments need to be built or if the build
 * process can be skipped.
 *
 * @author matthi-g
 */
public interface DockerImageChecker {
    /**
     * Get all functions out of a list of function deployments, that have to be built because they
     * are not up-to-date.
     *
     * @param functionDeployments the list of function deployments
     * @return a Single that emits a set of functions that have to be built
     */
    Single<Set<Function>> getNecessaryFunctionBuilds(List<FunctionDeployment> functionDeployments);

    /**
     * Check if an image for of a function is up-to-date.
     *
     * @param imageName the name of the image
     * @param tag the tag of the image
     * @param lastFunctionUpdate the last update of the function
     * @return a Single that emits true if the image is up-to-date, else false
     */
    Single<Boolean> isUpToDate(String imageName, String tag, Date lastFunctionUpdate);
}

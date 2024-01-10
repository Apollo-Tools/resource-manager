package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

public class ScrapeTargetRepository extends Repository<Resource> {

    /**
     * Create an instance.
     */
    public ScrapeTargetRepository() {
        super(Resource.class);
    }

    /**
     * Find all function deployment scrape targets.
     *
     * @param sessionManager the database session
     * @return a Maybe that emits the resource if it exists, else null
     */
    public Single<List<FindAllFunctionDeploymentScrapeTargetsDTO>> findAllFunctionDeploymentTargets(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select new at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO(" +
                    "fd.resourceDeploymentId, fd.deployment.deploymentId, fd.resource.resourceId, fd.directTriggerUrl" +
                    ") from FunctionDeployment fd " +
                    "where fd.status.statusValue=:statusDeployed and fd.resource.platform.platform=:platformEc2",
                FindAllFunctionDeploymentScrapeTargetsDTO.class)
            .setParameter("statusDeployed", DeploymentStatusValue.DEPLOYED.getValue())
            .setParameter("platformEc2", PlatformEnum.EC2.getValue())
            .getResultList()
        );
    }

    /**
     * Find all OpenFaaS scrape targets.
     *
     * @param sessionManager the database session
     * @return a Maybe that emits the resource if it exists, else null
     */
    public Single<List<FindAllOpenFaaSScrapeTargetsDTO>> findAllOpenFaaSTargets(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select new at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO(" +
                    "r.resourceId, " +
                    "(select mv.valueString from MetricValue mv where mv.metric.metric='base-url' " +
                        "and mv.resource.resourceId=r.resourceId), " +
                    "(select mv.valueNumber from MetricValue mv where mv.metric.metric='metrics-port' " +
                        "and mv.resource.resourceId=r.resourceId) " +
                    ") from Resource r " +
                    "where r.platform.platform=:platformOpenFaaS",
                FindAllOpenFaaSScrapeTargetsDTO.class)
            .setParameter("platformOpenFaaS", PlatformEnum.OPENFAAS.getValue())
            .getResultList()
        );
    }
}

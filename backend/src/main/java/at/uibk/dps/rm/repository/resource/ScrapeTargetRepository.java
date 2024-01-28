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
import java.util.Set;

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
    public Single<Set<FindAllFunctionDeploymentScrapeTargetsDTO>> findAllFunctionDeploymentTargets(
            SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct " +
                    "new at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO(" +
                    "fd.resourceDeploymentId, fd.deployment.deploymentId, fd.resource.resourceId, fd.baseUrl, " +
                    "fd.metricsPort) from FunctionDeployment fd " +
                    "where fd.status.statusValue=:statusDeployed and fd.resource.platform.platform=:platformEc2",
                FindAllFunctionDeploymentScrapeTargetsDTO.class)
            .setParameter("statusDeployed", DeploymentStatusValue.DEPLOYED.getValue())
            .setParameter("platformEc2", PlatformEnum.EC2.getValue())
            .getResultList()
            .thenApply(Set::copyOf)
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
            .createQuery("select distinct new at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO(" +
                    "r.resourceId, " +
                    "(select mv.valueString from MetricValue mv where mv.metric.metric='base-url' " +
                        "and mv.resource.resourceId=r.resourceId), " +
                    "(select mv.valueNumber from MetricValue mv where mv.metric.metric='metrics-port' " +
                        "and mv.resource.resourceId=r.resourceId) " +
                    ") from Resource r " +
                    "left join r.metricValues mv " +
                    "where r.platform.platform=:platformOpenFaaS",
                FindAllOpenFaaSScrapeTargetsDTO.class)
            .setParameter("platformOpenFaaS", PlatformEnum.OPENFAAS.getValue())
            .getResultList()
        );
    }
}

package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.service.database.util.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Utility class to mock (mocked construction) database util objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class DatabaseUtilMockprovider {
    public static MockedConstruction<MetricValueUtility> mockMetricValueUtilitySave(SessionManager sm,
                                                                                    Resource resource, JsonArray data) {
        return Mockito.mockConstruction(MetricValueUtility.class,
            (mock, context) -> given(mock.checkAddMetricList(sm, resource, data)).willReturn(Completable.complete()));
    }

    public static MockedConstruction<K8sResourceUpdateUtility> mockK8sResourceUpdateUtility(SessionManager sm,
            MainResource cluster, K8sMonitoringData data) {
        return Mockito.mockConstruction(K8sResourceUpdateUtility.class,
            (mock, context) -> given(mock.updateClusterNodes(sm, cluster, data)).willReturn(Completable.complete()));
    }

    public static MockedConstruction<EnsembleUtility> mockEnsembleUtilityFetch(SessionManager sm, long ensembleId,
                                                                               long accountId, GetOneEnsemble result) {
        return Mockito.mockConstruction(EnsembleUtility.class, (mock, context) ->
            given(mock.fetchAndPopulateEnsemble(sm, ensembleId, accountId)).willReturn(Single.just(result)));
    }

    public static MockedConstruction<EnsembleUtility> mockEnsembleUtilityFetchAndValidate(SessionManager sm,
            long ensembleId, long accountId, GetOneEnsemble fetchResult) {
        return Mockito.mockConstruction(EnsembleUtility.class, (mock, context) ->
            given(mock.fetchAndPopulateEnsemble(sm, ensembleId, accountId)).willReturn(Single.just(fetchResult)));
    }

    public static MockedConstruction<SLOUtility> mockSLOUtilityFindAndFilterResources(SessionManager sm,
            List<Resource> result) {
        return Mockito.mockConstruction(SLOUtility.class, (mock, context) ->
            given(mock.findResourcesByNonMonitoredSLOs(eq(sm), any(SLORequest.class))).willReturn(Single.just(result)));
    }

    public static MockedConstruction<EnsembleValidationUtility> mockEnsembleValidationUtility(SessionManager sm,
            long ensembleId, long accountId, List<ResourceEnsembleStatus> result) {
        return Mockito.mockConstruction(EnsembleValidationUtility.class, (mock, context) ->
            given(mock.validateAndUpdateEnsemble(sm, ensembleId, accountId)).willReturn(Single.just(result)));
    }

    public static MockedConstruction<EnsembleValidationUtility> mockEnsembleValidationUtilityList(SessionManager sm,
            long accountId, Map<Ensemble, List<ResourceEnsembleStatus>> result) {
        return Mockito.mockConstruction(EnsembleValidationUtility.class, (mock, context) ->
            result.forEach((key, value) -> given(mock.validateAndUpdateEnsemble(sm, key.getEnsembleId(), accountId))
                .willReturn(Single.just(value))));
    }

    public static MockedConstruction<EnsembleValidationUtility> mockEnsembleValidationUtility(SessionManager sm,
            long ensembleId, List<ResourceEnsembleStatus> statusValues) {
        return Mockito.mockConstruction(EnsembleValidationUtility.class, (mock, context) ->
            given(mock.updateResourceEnsembleStatuses(sm, ensembleId, statusValues)).willReturn(Completable.complete()));
    }

    public static MockedConstruction<DeploymentUtility> mockDeploymentUtility(SessionManager sm) {
        return Mockito.mockConstruction(DeploymentUtility.class, (mock, context) ->
            given(mock.mapResourceDeploymentsToDTO(eq(sm), any(DeployTerminateDTO.class)))
                .willReturn(Completable.complete()));
    }

    public static MockedConstruction<DeploymentValidationUtility> mockDeploymentValidationUtilityValid(
            SessionManager sm, List<Resource> result) {
        return Mockito.mockConstruction(DeploymentValidationUtility.class, (mock, context) ->
            given(mock.checkDeploymentIsValid(eq(sm), any(DeployResourcesRequest.class), any(DeployResourcesDTO.class)))
                .willReturn(Single.just(result)));
    }

    public static MockedConstruction<DeploymentValidationUtility> mockDeploymentValidationUtilityValid(
            SessionManager sm, List<Resource> result, long accountId) {
        return Mockito.mockConstruction(DeploymentValidationUtility.class, (mock, context) -> {
            given(mock.checkDeploymentIsValid(eq(sm), any(DeployResourcesRequest.class), any(DeployResourcesDTO.class)))
                .willReturn(Single.just(result));
            given(mock.checkEnsembleResourcesForAlerting(eq(sm), any(DeployResourcesRequest.class),
                any(Deployment.class), eq(accountId))).willReturn(Single.just(1L));
        });
    }

    public static MockedConstruction<SaveResourceDeploymentUtility> mockSaveResourceDeploymentUtility(
        SessionManager sm, ResourceDeploymentStatus status, List<K8sNamespace> namespaces,
        List<Resource> resources) {
        return Mockito.mockConstruction(SaveResourceDeploymentUtility.class, (mock, context) -> {
            given(mock.saveFunctionDeployments(eq(sm), any(Deployment.class), any(DeployResourcesRequest.class),
                eq(status), eq(resources))).willReturn(Completable.complete());
            given(mock.saveServiceDeployments(eq(sm), any(Deployment.class), any(DeployResourcesRequest.class),
                eq(status), eq(namespaces), eq(resources))).willReturn(Completable.complete());
        });
    }

    public static MockedConstruction<TriggerUrlUtility> mockTriggerUrlUtility(SessionManager sm) {
        return Mockito.mockConstruction(TriggerUrlUtility.class, (mock, context) -> {
            when(mock.setTriggerUrlsForFunctions(eq(sm), any(DeploymentOutput.class), any(DeployResourcesDTO.class)))
                .thenReturn(Completable.complete());
            when(mock.setTriggerUrlForServices(eq(sm), any(DeployResourcesDTO.class)))
                .thenReturn(Completable.complete());
        });
    }

    public static MockedConstruction<LockedResourcesUtility> mockLockUtilityLockResources(SessionManager sm,
            List<ResourceId> lockResources) {
        return Mockito.mockConstruction(LockedResourcesUtility.class, (mock, context) ->
            given(mock.lockResources(eq(sm), eq(lockResources), any(Deployment.class)))
                .willReturn(Single.just(List.of())));
    }

    public static MockedConstruction<LockedResourcesUtility> mockLockUtilityUnlockResources(SessionManager sm,
            Long deploymentId) {
        return Mockito.mockConstruction(LockedResourcesUtility.class, (mock, context) ->
            given(mock.unlockDeploymentResources(sm, deploymentId)).willReturn(Completable.complete()));
    }
}

package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * A utility class that provides various methods to validate ensembles.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class EnsembleValidationUtility {

    private final EnsembleRepositoryProvider repositoryProvider;

    /**
     * Validate an existing ensemble and update the validity of all resource deployments of the
     * ensemble.
     *
     * @param sm a database session manager
     * @param ensembleId the id of the ensemble
     * @param accountId the id of the account
     * @return a Single that emits all status values of the resource deployments of the ensemble
     */
    public Single<List<ResourceEnsembleStatus>> validateAndUpdateEnsemble(SessionManager sm, long ensembleId,
            long accountId) {
        EnsembleUtility ensembleUtility = new EnsembleUtility(repositoryProvider);
        SLOUtility sloUtility = new SLOUtility(repositoryProvider.getResourceRepository());
        return repositoryProvider.getEnsembleRepository().findByIdAndAccountId(sm, ensembleId, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> ensembleUtility.fetchAndPopulateEnsemble(sm, ensembleId, accountId))
            .flatMap(getOneEnsemble -> sloUtility.findResourcesByNonMonitoredSLOs(sm, getOneEnsemble)
                .map(validResources -> EnsembleUtility.getResourceEnsembleStatus(validResources,
                    getOneEnsemble.getResources()))
            )
            .flatMap(statusValues -> updateResourceEnsembleStatuses(sm, ensembleId, statusValues)
                .andThen(Single.defer(() -> Single.just(statusValues)))
            );
    }

    /**
     * Update the status values of a resource ensemble.
     *
     * @param sm the database session manager
     * @param ensembleId the id of the ensemble
     * @param statusValues the new status values
     * @return a Completable
     */
    public Completable updateResourceEnsembleStatuses(SessionManager sm, long ensembleId,
            List<ResourceEnsembleStatus> statusValues) {
        return Observable.fromIterable(statusValues)
            .map(ResourceEnsembleStatus::getIsValid)
            .reduce((status1, status2) -> status1 && status2)
            .flatMapCompletable(status -> repositoryProvider.getEnsembleRepository()
                .updateValidity(sm, ensembleId, status));
    }
}

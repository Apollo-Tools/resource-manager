package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to instantiate objects that are linked to the ensemble entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestEnsembleProvider {

    public static Ensemble createEnsemble(long ensembleId, long accountId, String name) {
        Ensemble ensemble = new Ensemble();
        ensemble.setEnsembleId(ensembleId);
        ensemble.setName(name);
        ensemble.setCreatedBy(TestAccountProvider.createAccount(accountId));
        ensemble.setIsValid(true);
        ensemble.setRegions(List.of(1L, 2L));
        ensemble.setProviders(List.of(3L, 4L));
        ensemble.setResource_types(List.of(3L, 4L));
        ensemble.setEnvironments(List.of(5L));
        ensemble.setPlatforms(List.of(1L, 5L));
        return ensemble;
    }

    public static Ensemble createEnsemble(long ensembleId, long accountId) {
        String name = "ensemble" + ensembleId;
        return createEnsemble(ensembleId, accountId, name);
    }

    public static EnsembleSLO createEnsembleSLO(long ensembleSLOId, String name, long ensembleId) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.EQ);
        ensembleSLO.setValueNumbers(List.of(0.5, 1.36));
        Ensemble ensemble = createEnsemble(ensembleId, 1L);
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static EnsembleSLO createEnsembleSLO(long ensembleSLOId, String name, long ensembleId, String value) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.EQ);
        ensembleSLO.setValueStrings(List.of(value));
        Ensemble ensemble = createEnsemble(ensembleId, 1L);
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static EnsembleSLO createEnsembleSLOGT(long ensembleSLOId, String name, long ensembleId, double value) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.GT);
        ensembleSLO.setValueNumbers(List.of(value));
        Ensemble ensemble = createEnsemble(ensembleId, 1L);
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static EnsembleSLO createEnsembleSLO(long ensembleSLOId, String name, long ensembleId, Boolean... values) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.EQ);
        ensembleSLO.setValueBools(Arrays.stream(values).collect(Collectors.toList()));
        Ensemble ensemble = createEnsemble(ensembleId, 1L);
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static ResourceEnsemble createResourceEnsemble(long resourceEnsembleId, Ensemble ensemble,
            Resource resource) {
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        resourceEnsemble.setResourceEnsembleId(resourceEnsembleId);
        resourceEnsemble.setEnsemble(ensemble);
        resourceEnsemble.setResource(resource);
        return resourceEnsemble;
    }
}

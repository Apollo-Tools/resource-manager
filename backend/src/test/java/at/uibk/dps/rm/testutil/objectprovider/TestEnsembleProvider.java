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

    public static Ensemble createEnsemble(Long ensembleId, long accountId, String name, boolean isValid,
            List<Long> regions, List<Long> providers, List<Long> resourceTypes, List<Long> environments,
            List<Long> platforms) {
        Ensemble ensemble = new Ensemble();
        ensemble.setEnsembleId(ensembleId);
        ensemble.setName(name);
        ensemble.setCreatedBy(TestAccountProvider.createAccount(accountId));
        ensemble.setIsValid(isValid);
        ensemble.setRegions(regions);
        ensemble.setProviders(providers);
        ensemble.setResource_types(resourceTypes);
        ensemble.setEnvironments(environments);
        ensemble.setPlatforms(platforms);
        return ensemble;
    }

    public static Ensemble createEnsemble(Long ensembleId, long accountId, String name, boolean isValid) {
        return createEnsemble(ensembleId, accountId, name, isValid, List.of(1L, 2L), List.of(3L, 4L), List.of(3L, 4L),
            List.of(5L), List.of(1L, 5L));
    }

    public static Ensemble createEnsemble(Long ensembleId, long accountId, String name) {
        return createEnsemble(ensembleId, accountId, name, true);
    }

    public static Ensemble createEnsemble(long ensembleId, long accountId) {
        String name = "ensemble" + ensembleId;
        return createEnsemble(ensembleId, accountId, name);
    }

    public static Ensemble createEnsembleNoSLOs(long ensembleId) {
        Ensemble ensemble = new Ensemble();
        ensemble.setEnsembleId(ensembleId);
        ensemble.setName("ensemble" + ensembleId);
        ensemble.setRegions(List.of());
        ensemble.setProviders(List.of());
        ensemble.setResource_types(List.of());
        ensemble.setPlatforms(List.of());
        ensemble.setEnvironments(List.of());
        return ensemble;
    }

    public static EnsembleSLO createEnsembleSLO(Long ensembleSLOId, String name, Ensemble ensemble, String value) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.EQ);
        ensembleSLO.setValueStrings(List.of(value));
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static EnsembleSLO createEnsembleSLO(Long ensembleSLOId, String name, long ensembleId, String value) {
        return createEnsembleSLO(ensembleSLOId, name, createEnsemble(ensembleId, 1L), value);
    }

    public static EnsembleSLO createEnsembleSLOGT(Long ensembleSLOId, String name, Ensemble ensemble, double value) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.GT);
        ensembleSLO.setValueNumbers(List.of(value));
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static EnsembleSLO createEnsembleSLOGT(Long ensembleSLOId, String name, long ensembleId, double value) {
        return createEnsembleSLOGT(ensembleSLOId, name, createEnsemble(ensembleId, 1L), value);
    }

    public static EnsembleSLO createEnsembleSLO(Long ensembleSLOId, String name, Ensemble ensemble, Boolean... values) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setEnsembleSLOId(ensembleSLOId);
        ensembleSLO.setName(name);
        ensembleSLO.setExpression(ExpressionType.EQ);
        ensembleSLO.setValueBools(Arrays.stream(values).collect(Collectors.toList()));
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    public static EnsembleSLO createEnsembleSLO(Long ensembleSLOId, String name, long ensembleId, Boolean... values) {
        return createEnsembleSLO(ensembleSLOId, name, createEnsemble(ensembleId, 1L), values);
    }

    public static ResourceEnsemble createResourceEnsemble(Long resourceEnsembleId, Ensemble ensemble,
            Resource resource) {
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        resourceEnsemble.setResourceEnsembleId(resourceEnsembleId);
        resourceEnsemble.setEnsemble(ensemble);
        resourceEnsemble.setResource(resource);
        return resourceEnsemble;
    }
}

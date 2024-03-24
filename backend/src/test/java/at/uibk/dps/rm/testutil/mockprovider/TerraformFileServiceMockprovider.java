package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.service.deployment.terraform.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Utility class to mock (mocked construction) terraform executor objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class TerraformFileServiceMockprovider {
    public static MockedConstruction<FunctionPrepareService> mockFunctionPrepareService(FunctionsToDeploy functionsToDeploy) {
        return Mockito.mockConstruction(FunctionPrepareService.class,
            (mock, context) -> given(mock.packageCode()).willReturn(Single.just(functionsToDeploy)));
    }

    public static MockedConstruction<MainFileService> mockMainFileService(Completable result) {
        return Mockito.mockConstruction(MainFileService.class,
            (mock, context) -> given(mock.setUpDirectory()).willReturn(result));
    }

    public static MockedConstruction<RegionFaasFileService> mockRegionFaasFileService(Completable result) {
        return Mockito.mockConstruction(RegionFaasFileService.class,
            (mock, context) -> when(mock.setUpDirectory()).thenReturn(result));
    }

    public static MockedConstruction<ServicePullFileService> mockContainerPullFileService(Completable result) {
        return Mockito.mockConstruction(ServicePullFileService.class, (mock, context) ->
            when(mock.setUpDirectory()).thenReturn(result));
    }

    public static MockedConstruction<ServiceDeployFileService> mockContainerDeployFileService(Completable result) {
        return Mockito.mockConstruction(ServiceDeployFileService.class, (mock, context) ->
            when(mock.setUpDirectory()).thenReturn(result));
    }
}

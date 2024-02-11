package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@UtilityClass
public class SLOMockProvider {

    public static MockedConstruction<SLOValidator> mockSLOValidatorFilter(JsonArray resources,
                                                                          Set<Resource> validResources) {
        return Mockito.mockConstruction(SLOValidator.class,
            (mock, context) -> when(mock.filterResourcesByMonitoredMetrics(resources))
                .thenReturn(Single.just(validResources)));
    }

    public static MockedConstruction<SLOValidator> mockSLOValidatorFilter(List<JsonArray> resources,
            List<Set<Resource>> validResources) {
        return Mockito.mockConstruction(SLOValidator.class,
            (mock, context) -> {
                for (int i = 0; i < resources.size(); i++) {
                    when(mock.filterResourcesByMonitoredMetrics(resources.get(i)))
                        .thenReturn(Single.just(validResources.get(i)));
                }
            });
    }

    public static MockedConstruction<SLOValidator> mockSLOValidatorFilterAndSort(JsonArray resources,
            Resource validResource1, Resource validResource2, int sortOrder) {
        return Mockito.mockConstruction(SLOValidator.class,
            (mock, context) -> {
                when(mock.filterResourcesByMonitoredMetrics(resources))
                    .thenReturn(Single.just(Set.of(validResource1, validResource2)));
                doReturn(sortOrder).when(mock).sortResourceBySLOs(argThat(
                    (Resource r1) -> r1!=null && r1.equals(validResource1)), eq(validResource2), anyList());
                doReturn(sortOrder * -1).when(mock).sortResourceBySLOs(
                    argThat((Resource r2) -> r2!=null && r2.equals(validResource2)), eq(validResource1), anyList());
            });
    }
}

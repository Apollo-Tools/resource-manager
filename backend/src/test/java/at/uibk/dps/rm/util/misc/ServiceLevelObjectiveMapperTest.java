package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link RxVertxHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceLevelObjectiveMapperTest {

    @Test
    void mapEnsembleSLONumber() {
        EnsembleSLO ensembleSLO = TestEnsembleProvider.createEnsembleSLOGT(1L, "availability",
            2L, 0.95);
        ServiceLevelObjective result = ServiceLevelObjectiveMapper.mapEnsembleSLO(ensembleSLO);

        assertThat(result.getName()).isEqualTo("availability");
        assertThat(result.getExpression()).isEqualTo(ExpressionType.GT);
        assertThat(result.getValue().get(0).getValueNumber()).isEqualTo(0.95);
    }

    @Test
    void mapEnsembleSLOString() {
        EnsembleSLO ensembleSLO = TestEnsembleProvider.createEnsembleSLO(1L, "os",
            2L, "linux");
        ServiceLevelObjective result = ServiceLevelObjectiveMapper.mapEnsembleSLO(ensembleSLO);

        assertThat(result.getName()).isEqualTo("os");
        assertThat(result.getExpression()).isEqualTo(ExpressionType.EQ);
        assertThat(result.getValue().get(0).getValueString()).isEqualTo("linux");
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "empty"})
    void mapEnsembleSLOBoolean(String otherLists) {
        EnsembleSLO ensembleSLO = TestEnsembleProvider.createEnsembleSLO(1L, "online",
            2L, true, false);
        ensembleSLO.setValueNumbers(otherLists.equals("null") ? null : List.of());
        ensembleSLO.setValueStrings(otherLists.equals("null") ? null : List.of());
        ServiceLevelObjective result = ServiceLevelObjectiveMapper.mapEnsembleSLO(ensembleSLO);

        assertThat(result.getName()).isEqualTo("online");
        assertThat(result.getExpression()).isEqualTo(ExpressionType.EQ);
        assertThat(result.getValue().get(0).getValueBool()).isEqualTo(true);
        assertThat(result.getValue().get(1).getValueBool()).isEqualTo(false);
    }
}

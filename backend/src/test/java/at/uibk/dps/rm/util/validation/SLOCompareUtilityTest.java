package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link SLOCompareUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
public class SLOCompareUtilityTest {

    private ServiceLevelObjective sloAvailability, sloLatency;

    @BeforeEach
    void initTest() {
        sloAvailability = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT,
            0.80);
        sloLatency = TestDTOProvider.createServiceLevelObjective("latency", ExpressionType.LT, 20);
    }

    private MetricValue setupMetricValue(Double valueNumber, String valueString, Boolean valueBoolean) {
        MetricValue metricValue = new MetricValue();
        if (valueNumber != null) {
            metricValue.setValueNumber(valueNumber);
        }
        if (valueString != null) {
            metricValue.setValueString(valueString);
        }
        if (valueBoolean != null) {
            metricValue.setValueBool(valueBoolean);
        }
        return metricValue;
    }

    private List<SLOValue> setupSLOValues(String... sloValues) {
        List<SLOValue> sloValueList = new ArrayList<>();
        for (String sloValue : sloValues) {
            SLOValue value = new SLOValue();
            value.setSloValueType(SLOValueType.STRING);
            value.setValueString(sloValue);
            sloValueList.add(value);
        }
        return sloValueList;
    }

    private List<SLOValue> setupSLOValues(Double valueNumber, Boolean valueBoolean) {
        SLOValue value = new SLOValue();
        if (valueNumber != null) {
            value.setSloValueType(SLOValueType.NUMBER);
            value.setValueNumber(valueNumber);
        } else if (valueBoolean != null) {
            value.setSloValueType(SLOValueType.BOOLEAN);
            value.setValueBool(valueBoolean);
        }
        return List.of(value);
    }

    @ParameterizedTest
    @CsvSource({
        "10.0, 8.3, GT, true",
        "8.3, 10.0, GT, false",
        "8.3, 8.3, GT, false",
        "10.0, 8.3, LT, false",
        "8.3, 10.0, LT, true",
        "8.3, 8.3, LT, false",
        "8.3, 8.3, EQ, true",
        "8.3, 10.0, EQ, false",
        "10.0, 8.3, EQ, false"
    })
    void compareMetricValueWithSloNumber(double metricValue, double sloValue, String symbol, boolean expectedValue) {
        MetricValue metric = setupMetricValue(metricValue, null, null);
        List<SLOValue> sloValues = setupSLOValues(sloValue, null);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.valueOf(symbol), sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "hello, hello, EQ, true",
            "hello, help, EQ, false",
            "hello, hello, GT, false",
            "hello, help, GT, false",
            "hello, hello, LT, false",
            "hello, help, LT, false",
    })
    void compareMetricValueWithOneSloString(String metricValue, String sloValue, String symbol, boolean expectedValue) {
        MetricValue metric = setupMetricValue(null, metricValue, null);
        List<SLOValue> sloValues = setupSLOValues(sloValue);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.valueOf(symbol), sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "hello, hellp, hellt, hell, EQ, false",
            "hello, hello, hellp, hell, EQ, true",
            "hello, hellp, hello, hell, EQ, true",
            "hello, hellp, hell, hello, EQ, true",
    })
    void compareMetricValueWithMultipleSloStrings(String metricValue, String sloValue1, String sloValue2,
                                                  String sloValue3, String symbol, boolean expectedValue) {
        MetricValue metric = setupMetricValue(null, metricValue, null);
        List<SLOValue> sloValues = setupSLOValues(sloValue1, sloValue2, sloValue3);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.valueOf(symbol), sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, EQ, true",
            "true, false, EQ, false",
            "false, true, EQ, false",
            "true, true, GT, false",
            "true, false, GT, false",
            "false, true, GT, false",
            "true, true, LT, false",
            "true, false, LT, false",
            "false, true, LT, false"
    })
    void compareMetricValueWithSloBoolean(boolean metricValue, boolean sloValue, String symbol, boolean expectedValue) {
        MetricValue metric = setupMetricValue(null, null, metricValue);
        List<SLOValue> sloValues = setupSLOValues(null, sloValue);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.valueOf(symbol), sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    void compareMetricValueInvalidTypesNumber() {
        MetricValue metric = setupMetricValue(null, null, false);
        List<SLOValue> sloValues = setupSLOValues(10.0, null);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.GT, sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(false);
    }

    @Test
    void compareMetricValueInvalidTypesString() {
        MetricValue metric = setupMetricValue(10.0, null, null);
        List<SLOValue> sloValues = setupSLOValues("hello");
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.GT, sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(false);
    }

    @Test
    void compareMetricValueInvalidTypesBool() {
        MetricValue metric = setupMetricValue(10.0, null, null);
        List<SLOValue> sloValues = setupSLOValues(null, false);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("slo",
            ExpressionType.GT, sloValues);

        boolean actualValue = SLOCompareUtility.compareMetricValueWithSLO(metric, slo);

        assertThat(actualValue).isEqualTo(false);
    }

    private static Stream<Arguments> provideMonitoredMetricValues() {
        MonitoredMetricValue mv1 = TestMetricProvider
            .createMonitoredMetricValue(MonitoringMetricEnum.AVAILABILITY, 0.95);
        MonitoredMetricValue mv2 = TestMetricProvider.createMonitoredMetricValue(MonitoringMetricEnum.UP, true);
        MonitoredMetricValue mv3 = TestMetricProvider
            .createMonitoredMetricValue(MonitoringMetricEnum.LATENCY, 0.15);

        return Stream.of(
            Arguments.of(Set.of(mv1, mv2, mv3), List.of(mv1, mv3, mv2)),
            Arguments.of(Set.of(mv3, mv2, mv1), List.of(mv1, mv3, mv2)),
            Arguments.of(Set.of(mv3, mv1, mv2), List.of(mv1, mv3, mv2))
        );
    }

    @ParameterizedTest
    @MethodSource("provideMonitoredMetricValues")
    void sortMonitoredMetricValuesBySLOs(Set<MonitoredMetricValue> monitoredMetricValues,
            List<MonitoredMetricValue> expectedResult) {

        List<MonitoredMetricValue> result = SLOCompareUtility.sortMonitoredMetricValuesBySLOs(monitoredMetricValues,
            List.of(sloAvailability, sloLatency));

        assertThat(result.size()).isEqualTo(3);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getMetric()).isEqualTo(expectedResult.get(i).getMetric());
        }
    }
}

package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link SLOCompareUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
public class SLOCompareUtilityTest {

    private ServiceLevelObjective sloAvailability, sloLatency, sloOnline;
    
    private Metric mAvailability, mLatency, mOnline;

    @BeforeEach
    void initTest() {
        sloAvailability = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT,
            0.80);
        sloOnline = TestDTOProvider.createServiceLevelObjective("online", ExpressionType.EQ, true, 
            false);
        sloLatency = TestDTOProvider.createServiceLevelObjective("latency", ExpressionType.LT, 20);
        mAvailability = TestMetricProvider.createMetric(1L, "availability");
        mLatency = TestMetricProvider.createMetric(2L, "latency");
        mOnline = TestMetricProvider.createMetric(3L, "online");
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

    @Test
    void resourceFilterBySLOValueTypeValid() {
        Resource resource = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", "high");
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = SLOCompareUtility.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isTrue();
    }

    @Test
    void resourceFilterBySLOValueTypeNoValueTypeMatch() {
        Resource resource = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = SLOCompareUtility.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isFalse();
    }

    @Test
    void resourceFilterBySLOValueTypeNoNameMatch() {
        Resource resource = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("bandwidth", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = SLOCompareUtility.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"noFilter", "filterValid", "filterInvalid"})
    void resourceValidByNonMetricSLOSRegions(String type) {
        Region r1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Resource resource = TestResourceProvider.createResource(1L, r1);
        Ensemble ensemble = TestEnsembleProvider.createEnsembleNoSLOs(1L);
        switch (type) {
            case "filterValid":
                ensemble.setRegions(List.of(1L, 2L));
                break;
            case "filterInvalid":
                ensemble.setRegions(List.of(2L));
                break;
        }

        boolean result = SLOCompareUtility.resourceValidByNonMetricSLOS(resource, ensemble);

        assertThat(result).isEqualTo(!type.equals("filterInvalid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noFilter", "filterValid", "filterInvalid"})
    void resourceValidByNonMetricSLOSProviders(String type) {
        ResourceProvider rp1 = TestResourceProviderProvider.createResourceProvider(3L);
        Region r1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp1);
        Resource resource = TestResourceProvider.createResource(1L, r1);
        Ensemble ensemble = TestEnsembleProvider.createEnsembleNoSLOs(1L);
        switch (type) {
            case "filterValid":
                ensemble.setProviders(List.of(3L, 4L));
                break;
            case "filterInvalid":
                ensemble.setProviders(List.of(4L));
                break;
        }

        boolean result = SLOCompareUtility.resourceValidByNonMetricSLOS(resource, ensemble);

        assertThat(result).isEqualTo(!type.equals("filterInvalid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noFilter", "filterValid", "filterInvalid"})
    void resourceValidByNonMetricSLOSResourceTypes(String type) {
        ResourceType rt1 = TestResourceProvider.createResourceType(5L, "faas");
        Platform p1 = TestPlatformProvider.createPlatform(1L, "lambda", rt1);
        Resource resource = TestResourceProvider.createResource(1L, p1);
        Ensemble ensemble = TestEnsembleProvider.createEnsembleNoSLOs(1L);
        switch (type) {
            case "filterValid":
                ensemble.setResource_types(List.of(5L, 6L));
                break;
            case "filterInvalid":
                ensemble.setResource_types(List.of(6L));
                break;
        }

        boolean result = SLOCompareUtility.resourceValidByNonMetricSLOS(resource, ensemble);

        assertThat(result).isEqualTo(!type.equals("filterInvalid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noFilter", "filterValid", "filterInvalid"})
    void resourceValidByNonMetricSLOSPlatforms(String type) {
        ResourceType rt1 = TestResourceProvider.createResourceType(1L, "faas");
        Platform p1 = TestPlatformProvider.createPlatform(7L, "lambda", rt1);
        Resource resource = TestResourceProvider.createResource(1L, p1);
        Ensemble ensemble = TestEnsembleProvider.createEnsembleNoSLOs(1L);
        switch (type) {
            case "filterValid":
                ensemble.setPlatforms(List.of(7L, 8L));
                break;
            case "filterInvalid":
                ensemble.setPlatforms(List.of(8L));
                break;
        }

        boolean result = SLOCompareUtility.resourceValidByNonMetricSLOS(resource, ensemble);

        assertThat(result).isEqualTo(!type.equals("filterInvalid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noFilter", "filterValid", "filterInvalid"})
    void resourceValidByNonMetricSLOSEnvironments(String type) {
        Environment e1 = TestResourceProviderProvider.createEnvironment(9L, "cloud");
        ResourceProvider rp1 = TestResourceProviderProvider.createResourceProvider(1L, "aws", e1);
        Region r1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp1);
        Resource resource = TestResourceProvider.createResource(1L, r1);
        Ensemble ensemble = TestEnsembleProvider.createEnsembleNoSLOs(1L);
        switch (type) {
            case "filterValid":
                ensemble.setEnvironments(List.of(9L, 10L));
                break;
            case "filterInvalid":
                ensemble.setEnvironments(List.of(10L));
                break;
        }

        boolean result = SLOCompareUtility.resourceValidByNonMetricSLOS(resource, ensemble);

        assertThat(result).isEqualTo(!type.equals("filterInvalid"));
    }

    @Test
    void filterAndSortResourcesBySLOsDifferentNumbers() {
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, mAvailability, 0.95);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, mAvailability, 0.75);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, mAvailability, 0.95);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, mLatency, 15);
        MetricValue mv5 = TestMetricProvider.createMetricValue(5L, mLatency, 10);
        MetricValue mv6 = TestMetricProvider.createMetricValue(6L, mLatency, 12);
        MetricValue mAll = TestMetricProvider.createMetricValue(4L, mOnline, false);
        Resource r1 = TestResourceProvider.createResource(1L, mv4, mv1, mAll);
        Resource r2 = TestResourceProvider.createResource(2L, mv2, mv5, mAll);
        Resource r3 = TestResourceProvider.createResource(3L, mv3, mv6, mAll);

        List<Resource> result = SLOCompareUtility.filterAndSortResourcesBySLOs(List.of(r1, r2, r3),
            List.of(sloAvailability, sloLatency));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(r3);
        assertThat(result.get(1)).isEqualTo(r1);
    }

    @Test
    void filterAndSortResourcesBySLOsSameNumbers() {
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, mAvailability, 0.95);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, mAvailability, 0.95);
        Resource r1 = TestResourceProvider.createResource(1L, mv1);
        Resource r2 = TestResourceProvider.createResource(2L, mv2);

        List<Resource> result = SLOCompareUtility.filterAndSortResourcesBySLOs(List.of(r1, r2),
            List.of(sloAvailability));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(r1);
        assertThat(result.get(1)).isEqualTo(r2);
    }

    @Test
    void filterAndSortResourcesBySLOsSameMixed() {
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, mAvailability, 0.95);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, mAvailability, 0.95);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, mOnline, false);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, mOnline, true);
        Resource r1 = TestResourceProvider.createResource(1L, mv3, mv1);
        Resource r2 = TestResourceProvider.createResource(2L, mv2, mv4);

        List<Resource> result = SLOCompareUtility.filterAndSortResourcesBySLOs(List.of(r1, r2),
            List.of(sloAvailability, sloOnline));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(r1);
        assertThat(result.get(1)).isEqualTo(r2);
    }
}

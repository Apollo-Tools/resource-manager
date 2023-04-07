package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricValueMapperTest {

    @Test
    void mapMetricValues() {
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "m1", 11.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "m2", 22.0);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 3L, "m3", 33.0);

        Map<String, MetricValue> result = MetricValueMapper.mapMetricValues(Set.of(mv1, mv2, mv3));

        assertThat(result.get("m1").getMetricValueId()).isEqualTo(1L);
        assertThat(result.get("m2").getMetricValueId()).isEqualTo(2L);
        assertThat(result.get("m3").getMetricValueId()).isEqualTo(3L);
    }
}

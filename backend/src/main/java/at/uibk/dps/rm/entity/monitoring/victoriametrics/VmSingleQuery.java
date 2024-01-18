package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class VmSingleQuery implements VmQuery {

    private final String metric;

    private Map<String, Set<String>> filter = Map.of();

    private String timeRange = null;

    private String[] sortByNumericLabels = new String[]{};

    private String[] sortByLabels = new String[]{};

    private boolean sortAsc = true;

    private double multiplier = 1.0;

    private double summand = 0.0;

    public VmSingleQuery setFilter(Map<String, Set<String>> filter) {
        this.filter = filter;
        return this;
    }

    public VmSingleQuery setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        return this;
    }

    public VmSingleQuery setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
        return this;
    }

    public VmSingleQuery setSortByNumericLabels(String... sortByNumericLabels) {
        this.sortByNumericLabels = sortByNumericLabels;
        this.sortByLabels = new String[]{};
        return this;
    }

    public VmSingleQuery setSortByLabels(String... sortByLabels) {
        this.sortByLabels = sortByLabels;
        this.sortByNumericLabels = new String[]{};
        return this;
    }

    public VmSingleQuery setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public VmSingleQuery setSummand(Double summand) {
        this.summand = summand;
        return this;
    }

    @Override
    public String toString() {
        String metricString = metric +
            (filter.isEmpty() ? "" :
            "{" +
            filter.entrySet().stream()
                .map(entry -> entry.getKey() + "=~\"" + String.join("|", entry.getValue()) + "\"")
                .collect(Collectors.joining(","))
            + "}"
            ) +
            (timeRange == null ? "" : "[" + timeRange + "]") +
            (multiplier == 1.0 ? "" : "*" + multiplier) +
            (summand == 0.0 ? "" : "+" + summand);
        String fullQuery;
        if (sortByNumericLabels.length > 0) {
            fullQuery = "sort_by_label_numeric" + (sortAsc ? "(" : "_desc(") + metricString + ',' +
                mapSortArrayToString(sortByNumericLabels) + ")";
        } else if (sortByLabels.length > 0) {
            fullQuery = "sort_by_label" + (sortAsc ? "(" : "_desc(") + metricString + ',' +
                mapSortArrayToString(sortByLabels) + ")";
        } else {
            fullQuery = metricString;
        }
        return fullQuery;
    }

    private String mapSortArrayToString(String[] sortArray) {
        return Arrays.stream(sortArray).map(label -> "\"" + label + "\"").collect(Collectors.joining(","));
    }
}

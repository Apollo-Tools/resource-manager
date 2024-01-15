package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class VmQuery {

    private final String metric;

    private Map<String, List<String>> filter = Map.of();

    private String timeRange = null;

    private String[] sortByNumericLabels = new String[]{};

    private String[] sortByLabels = new String[]{};

    private boolean sortAsc = true;

    public VmQuery setFilter(Map<String, List<String>> filter) {
        this.filter = filter;
        return this;
    }

    public VmQuery setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        return this;
    }

    public VmQuery setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
        return this;
    }

    public VmQuery setSortByNumericLabels(String... sortByNumericLabels) {
        this.sortByNumericLabels = sortByNumericLabels;
        this.sortByLabels = new String[]{};
        return this;
    }

    public VmQuery setSortByLabels(String... sortByLabels) {
        this.sortByLabels = sortByLabels;
        this.sortByNumericLabels = new String[]{};
        return this;
    }

    @Override
    public String toString() {
        String metricString = metric + "{" +
            filter.entrySet().stream()
                .map(entry -> entry.getKey() + "=~\"" + String.join("|", entry.getValue()) + "\"")
                .collect(Collectors.joining(","))
            + "}" +
            (timeRange == null ? "" : "[" + timeRange + "]");
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

package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements a single <a href=https://victoriametrics.com/>VictoriaMetrics</a> query to select a
 * metric using a filter, timeRange add sorting and modify the result with an addition or
 * multiplication.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
@Getter
public class VmSingleQuery implements VmQuery {

    private final String metric;

    private Set<VmFilter> filter = Set.of();

    private String timeRange = null;

    private String[] sortByNumericLabels = new String[]{};

    private String[] sortByLabels = new String[]{};

    private boolean sortAsc = true;

    private double multiplier = 1.0;

    private double summand = 0.0;

    /**
     * Set the filter of the query and return the current instance.
     *
     * @param filter the filter
     * @return the current instance
     */
    public VmSingleQuery setFilter(Set<VmFilter> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set the time range of the query and return the current instance.
     *
     * @param timeRange the time range
     * @return the current instance
     */
    public VmSingleQuery setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        return this;
    }

    /**
     * Sort the result in ascending order or not.
     *
     * @param sortAsc whether to sort in ascending order
     * @return the current instance
     */
    public VmSingleQuery setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
        return this;
    }

    /**
     * Sort the result by one or more numeric labels. Numeric labels are labels where the value
     * is a number formatted as string.
     *
     * @param sortByNumericLabels the labels
     * @return the current instance
     */
    public VmSingleQuery setSortByNumericLabels(String... sortByNumericLabels) {
        this.sortByNumericLabels = sortByNumericLabels;
        this.sortByLabels = new String[]{};
        return this;
    }

    /**
     * Sort the result by one or more labels.
     *
     * @param sortByLabels the labels
     * @return the current instance
     */
    public VmSingleQuery setSortByLabels(String... sortByLabels) {
        this.sortByLabels = sortByLabels;
        this.sortByNumericLabels = new String[]{};
        return this;
    }

    /**
     * Set the multiplier of the query and return the current instance.
     *
     * @param multiplier the multiplier
     * @return the current instance
     */
    public VmSingleQuery setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    /**
     * Set the summand of the query and return the current instance.
     *
     * @param summand the multiplier
     * @return the current instance
     */
    public VmSingleQuery setSummand(Double summand) {
        this.summand = summand;
        return this;
    }

    @Override
    public String toString() {
        String metricString = metric +
            (filter.isEmpty() ? "" :
            "{" +
            filter.stream()
                .map(entry -> entry.getLabel() + entry.getOperator() + "\"" + String.join("|", entry.getValues()) + "\"")
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

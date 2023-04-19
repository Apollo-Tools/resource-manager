package at.uibk.dps.rm.entity.dto.slo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Represents the available expression types for Service Level Objectives in the
 * listFunctionResourcesBySLOs operation.
 *
 * @author matthi-g
 */
public enum ExpressionType {
    /**
     * Greater than
     */
    GT(">"),
    /**
     * Less than
     */
    LT("<"),
    /**
     * Equals
     */
    EQ("==");

    private final String symbol;

    /**
     * Create an instance from the symbol as string that corresponds to the ExpressionType.
     *
     * @param symbol the symbol
     */
    ExpressionType(final String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    public String getSymbol()
    {
        return this.symbol;
    }

    /**
     * Check whether a string symbol is a valid ExpressionType.
     *
     * @param symbol the symbol
     * @return true if the symbol is a valid ExpressionType, else false
     */
    public static Boolean symbolExists(final String symbol) {
        final ExpressionType[] expressionTypes = ExpressionType.values();
        for (final ExpressionType expressionType: expressionTypes) {
            if (expressionType.getSymbol().equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create an instance from a string symbol. This is necessary because a public is not allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param symbol the symbol
     * @return the created object
     */
    public static ExpressionType fromString(final String symbol) {
        return Arrays.stream(ExpressionType.values())
                .filter(value -> value.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + symbol));
    }

    /**
     * Compare two values based on the expression type.
     *
     * @param expressionType the expression type
     * @param value1 the first value
     * @param value2 the second value
     * @return 1, 0 or -1 depending on the expression type and values
     */
    public static int compareValues(final ExpressionType expressionType, final Double value1, final Double value2) {
        switch (expressionType) {
            case GT:
                return - value1.compareTo(value2);
            case LT:
            case EQ:
                return value1.compareTo(value2);
            default:
                return 0;
        }
    }

    /**
     * @see #compareValues(ExpressionType, Double, Double) 
     */
    public static int compareValues(final ExpressionType expressionType, final String value1, final String value2) {
        if (expressionType == ExpressionType.EQ) {
            return value1.compareTo(value2);
        }
        return -1;
    }

    /**
     * @see #compareValues(ExpressionType, Double, Double) 
     */
    public static int compareValues(final ExpressionType expressionType, final Boolean value1, final Boolean value2) {
        if (expressionType == ExpressionType.EQ) {
            return value1.compareTo(value2);
        }
        return -1;
    }
}

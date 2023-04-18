package at.uibk.dps.rm.entity.dto.slo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ExpressionType {
    GT(">"), LT("<"), EQ("==");

    private final String symbol;

    ExpressionType(final String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    public String getSymbol()
    {
        return this.symbol;
    }

    public static Boolean symbolExists(String symbol) {
        final ExpressionType[] expressionTypes = ExpressionType.values();
        for (final ExpressionType expressionType: expressionTypes) {
            if (expressionType.getSymbol().equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    // ref: https://stackoverflow.com/a/45082346/13164629
    public static ExpressionType fromString(String str) {
        return Arrays.stream(ExpressionType.values())
                .filter(value -> value.symbol.equals(str))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + str));
    }

    public static int compareValues(ExpressionType expressionType, Double value1, Double value2) {
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

    public static int compareValues(ExpressionType expressionType, String value1, String value2) {
        if (expressionType == ExpressionType.EQ) {
            return value1.compareTo(value2);
        }
        return -1;
    }

    public static int compareValues(ExpressionType expressionType, Boolean value1, Boolean value2) {
        if (expressionType == ExpressionType.EQ) {
            return value1.compareTo(value2);
        }
        return -1;
    }
}

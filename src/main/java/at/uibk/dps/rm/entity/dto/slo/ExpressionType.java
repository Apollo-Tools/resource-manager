package at.uibk.dps.rm.entity.dto.slo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ExpressionType {
    GT(">"), LT("<"), EQ("==");

    private final String symbol;

    ExpressionType(String symbol)
    {
        this.symbol = symbol;
    }

    @JsonValue
    public String getSymbol()
    {
        return this.symbol;
    }

    public static Boolean symbolExists(String symbol) {
        ExpressionType[] expressionTypes = ExpressionType.values();
        for (ExpressionType expressionType: expressionTypes) {
            if (expressionType.getSymbol().equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    // ref: https://stackoverflow.com/a/45082346/13164629
    public static ExpressionType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(ExpressionType.values())
                .filter(v -> v.symbol.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }

    public static int compareValues(ExpressionType expressionType, Double v1, Double v2) {
        switch (expressionType) {
            case GT:
                return - v1.compareTo(v2);
            case LT:
            case EQ:
                return v1.compareTo(v2);
            default:
                return 0;
        }
    }

    public static int compareValues(ExpressionType expressionType, String v1, String v2) {
        if (expressionType == ExpressionType.EQ) {
            return v1.compareTo(v2);
        }
        return -1;
    }

    public static int compareValues(ExpressionType expressionType, Boolean v1, Boolean v2) {
        if (expressionType == ExpressionType.EQ) {
            return v1.compareTo(v2);
        }
        return -1;
    }
}

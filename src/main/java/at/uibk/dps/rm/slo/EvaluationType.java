package at.uibk.dps.rm.slo;

import java.util.Arrays;

public enum EvaluationType {
    GT(">"), LT("<"), EQ("==");

    private final String symbol;

    EvaluationType(String symbol)
    {
        this.symbol = symbol;
    }

    public String getSymbol()
    {
        return this.symbol;
    }

    public static Boolean symbolExists(String symbol) {
        EvaluationType[] evaluationTypes = EvaluationType.values();
        for (EvaluationType evaluationType: evaluationTypes) {
            if (evaluationType.getSymbol().equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    // ref: https://stackoverflow.com/a/45082346/13164629
    public static EvaluationType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(EvaluationType.values())
                .filter(v -> v.symbol.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }

    public static int compareValues(String symbol, Double v1, Double v2) {
        switch (EvaluationType.fromString(symbol)) {
            case GT:
                return - v1.compareTo(v2);
            case LT:
                return v1.compareTo(v2);
            case EQ:
            default:
                return 0;
        }
    }
}

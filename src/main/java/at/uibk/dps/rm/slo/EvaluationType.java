package at.uibk.dps.rm.slo;

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
}

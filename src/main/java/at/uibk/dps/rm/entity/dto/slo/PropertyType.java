package at.uibk.dps.rm.entity.dto.slo;

public enum PropertyType {
    PROPERTY, METRIC;

    public static Boolean propertyTypeExists(String symbol) {
        PropertyType[] propertyTypes = PropertyType.values();
        for (PropertyType propertyType: propertyTypes) {
            if (propertyType.name().equals(symbol.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}

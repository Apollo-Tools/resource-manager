package at.uibk.dps.rm.handler.property;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.property.PropertyService;

public class PropertyHandler extends ValidationHandler {
    public PropertyHandler(PropertyService propertyService) {
        super(new PropertyChecker(propertyService));
    }
}

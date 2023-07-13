package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.ResourceType;
import lombok.experimental.UtilityClass;

/**
 * Utility class to instantiate objects that are linked to the platform entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestPlatformProvider {

    public static Platform createPlatform(long id, String name, ResourceType resourceType) {
        Platform platform = new Platform();
        platform.setPlatformId(id);
        platform.setPlatform(name);
        platform.setResourceType(resourceType);
        return platform;
    }

    public static Platform createPlatformFaas(long id, String name) {
        ResourceType resourceType = TestResourceProvider.createResourceTypeFaas(11L);
        return createPlatform(id, name, resourceType);
    }

    public static Platform createPlatformContainer(long id, String name) {
        ResourceType resourceType = TestResourceProvider.createResourceTypeContainer(22L);
        return createPlatform(id, name, resourceType);
    }


}

package at.uibk.dps.rm.entity.deployment;

/**
 * Represents resource providers that are available for the automatic deployment of resources
 *
 * @author matthi-g
 */
public enum CloudProvider {
    /**
     * Amazon Web Services
     */
    AWS,
    /**
     * Microsoft Azure
     */
    AZURE,
    /**
     * Google Cloud Platform
     */
    GOOGLE_CLOUD,
    /**
     * IBM Cloud
     */
    IBM,
    /**
     * Edge Resources
     */
    EDGE,
    /**
     * Container Resources
     */
    CONTAINER,
}

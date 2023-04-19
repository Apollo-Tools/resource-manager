package at.uibk.dps.rm.entity.deployment;

/**
 * Represents the status resource reservations.
 *
 * @author matthi-g
 */
public enum ReservationStatusValue {
    /**
     * A reservation is new and about to be deployed
     */
    NEW,
    /**
     * An error occured during deployment/termination
     */
    ERROR,
    /**
     * The resource is deployed and ready
     */
    DEPLOYED,
    /**
     * Resource is in the process of being terminated
     */
    TERMINATING,
    /**
     * The resource is terminated
     */
    TERMINATED
}

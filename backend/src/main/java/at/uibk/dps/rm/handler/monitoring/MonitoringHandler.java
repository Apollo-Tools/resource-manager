package at.uibk.dps.rm.handler.monitoring;

/**
 * Classes that implement MonitoringHandler are responsible to monitor resources managed by the
 * resource manager.
 *
 * @author matthi-g
 */
public interface MonitoringHandler {

    /**
     * Start the monitoring loop.
     */
    void startMonitoringLoop();

    /**
     * Pause the monitoring loop.
     */
    void pauseMonitoringLoop();
}

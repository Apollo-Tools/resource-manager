package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the credentials entity.
 *
 * @author matthi-g
 */
@Deprecated
public class CredentialsHandler extends ValidationHandler {

    /**
     * Create an instance from the credentialsChecker.
     *
     * @param credentialsChecker the credentials checker
     */
    public CredentialsHandler(CredentialsChecker credentialsChecker) {
        super(credentialsChecker);
    }
}

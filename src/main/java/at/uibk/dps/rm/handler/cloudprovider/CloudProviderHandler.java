package at.uibk.dps.rm.handler.cloudprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.cloudprovider.CloudProviderService;

public class CloudProviderHandler extends ValidationHandler {
    public CloudProviderHandler(CloudProviderService cloudProviderService) {
        super(new CloudProviderChecker(cloudProviderService));
    }
}

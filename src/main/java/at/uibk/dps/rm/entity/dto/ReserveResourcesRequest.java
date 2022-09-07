package at.uibk.dps.rm.entity.dto;

import java.util.List;

public class ReserveResourcesRequest {
    private List<Long> resources;
    private boolean deployResources;

    public List<Long> getResources() {
        return resources;
    }

    public void setResources(List<Long> resources) {
        this.resources = resources;
    }

    public boolean isDeployResources() {
        return deployResources;
    }

    public void setDeployResources(boolean deployResources) {
        this.deployResources = deployResources;
    }
}

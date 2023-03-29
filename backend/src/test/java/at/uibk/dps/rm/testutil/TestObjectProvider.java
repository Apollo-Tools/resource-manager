package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TestObjectProvider {

    public static Account createAccount(long accountId) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername("username");
        account.setPassword("password");
        account.setIsActive(true);
        return account;
    }

    public static Account createAccount(long accountId, String username, String password) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername(username);
        account.setPassword(password);
        account.setIsActive(true);
        return account;
    }

    public static Credentials createCredentials(long credentialsId, ResourceProvider resourceProvider) {
        Credentials credentials = new Credentials();
        credentials.setCredentialsId(credentialsId);
        credentials.setAccessKey("accesskey");
        credentials.setSecretAccessKey("secretaccesskey");
        credentials.setSessionToken("sessiontoken");
        credentials.setResourceProvider(resourceProvider);
        return credentials;
    }

    public static ResourceProvider createResourceProvider(long providerId) {
        ResourceProvider resourceProvider = new ResourceProvider();
        resourceProvider.setProviderId(providerId);
        resourceProvider.setProvider("aws");
        return resourceProvider;
    }

    public static ResourceProvider createResourceProvider(long providerId, String provider) {
        ResourceProvider resourceProvider = createResourceProvider(providerId);
        resourceProvider.setProvider(provider);
        return resourceProvider;
    }

    public static AccountCredentials createAccountCredentials(long accountCredentialsId, Account account,
                                                              Credentials credentials) {
        AccountCredentials accountCredentials = new AccountCredentials();
        accountCredentials.setAccountCredentialsId(accountCredentialsId);
        accountCredentials.setAccount(account);
        accountCredentials.setCredentials(credentials);
        return accountCredentials;
    }

    public static Resource createResource(long id, ResourceType resourceType, Region region, boolean selfManaged) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setResourceType(resourceType);
        resource.setRegion(region);
        resource.setIsSelfManaged(selfManaged);
        return resource;
    }

    public static Resource createResource(long id, ResourceType resourceType, boolean selfManaged) {
        ResourceProvider resourceProvider = createResourceProvider(1L);
        Region region = createRegion(1L, "us-east-1", resourceProvider);
        return createResource(id, resourceType, region, selfManaged);
    }

    public static Resource createResource(long resourceId) {
        ResourceType resourceType = createResourceType(1L, "cloud");
        return createResource(resourceId, resourceType, true);
    }

    public static ResourceType createResourceType(long resourceTypeId, String resourceTypeLabel) {
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(resourceTypeId);
        resourceType.setResourceType(resourceTypeLabel);
        return resourceType;
    }

    public static MetricType createMetricType(long metricTypeId, String metricTypeName) {
        MetricType metricType = new MetricType();
        metricType.setMetricTypeId(metricTypeId);
        metricType.setType(metricTypeName);
        return metricType;
    }

    public static Metric createMetric(long metricId, String metricName, long metricTypeId, String metricType,
                                      boolean isMonitored) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(createMetricType(metricTypeId, metricType));
        metric.setDescription("Blah");
        metric.setIsMonitored(isMonitored);
        return metric;
    }


    public static Metric createMetric(long metricId, String metricName, MetricType metricType, boolean isMonitored) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(metricType);
        metric.setDescription("Blah");
        metric.setIsMonitored(isMonitored);
        return metric;
    }

    public static Metric createMetric(long metricId, String metricName) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(createMetricType(1L, "number"));
        metric.setDescription("Blah");
        metric.setIsMonitored(false);
        return metric;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, double value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, String value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric);
        metricValue.setValueString(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, boolean value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric);
        metricValue.setValueBool(value);
        return metricValue;
    }

    private static void initMetricValue(MetricValue metricValue, long metricValueId, long metricId, String metric) {
        metricValue.setMetricValueId(metricValueId);
        metricValue.setMetric(createMetric(metricId, metric));
        metricValue.setCount(10L);
    }

    public static SLOValue createSLOValue(double value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.NUMBER);
        sloValue.setValueNumber(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(String value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.STRING);
        sloValue.setValueString(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(boolean value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.BOOLEAN);
        sloValue.setValueBool(value);
        return sloValue;
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    double... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (double v : value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    String... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (String v : value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    boolean... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (boolean v : value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static GetResourcesBySLOsRequest createResourceBySLOsRequest(List<ServiceLevelObjective> slos,
                                                                        int limit) {
        GetResourcesBySLOsRequest request = new GetResourcesBySLOsRequest();
        request.setServiceLevelObjectives(slos);
        request.setLimit(limit);
        return request;
    }

    public static ResourceReservation createResourceReservation(long id, FunctionResource functionResource, Reservation reservation,
                                                                ResourceReservationStatus resourceReservationStatus) {
        ResourceReservation resourceReservation = new ResourceReservation();
        resourceReservation.setResourceReservationId(id);
        resourceReservation.setFunctionResource(functionResource);
        resourceReservation.setReservation(reservation);
        resourceReservation.setStatus(resourceReservationStatus);
        return resourceReservation;
    }

    public static Reservation createReservation(long id, boolean isActive, Account account) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(id);
        reservation.setIsActive(isActive);
        reservation.setCreatedBy(account);
        return  reservation;
    }

    public static FunctionResourceIds createFunctionResourceIds(long functionId, long resourceId) {
        FunctionResourceIds ids = new FunctionResourceIds();
        ids.setFunctionId(functionId);
        ids.setResourceId(resourceId);
        return ids;
    }

    public static FunctionResourceIds createFunctionResourceIds(FunctionResource functionResource) {
        return createFunctionResourceIds(functionResource.getFunction().getFunctionId(),
            functionResource.getResource().getResourceId());
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources,
                                                                        DockerCredentials dockerCredentials) {
        ReserveResourcesRequest request = new ReserveResourcesRequest();
        request.setFunctionResources(functionResources);
        request.setDockerCredentials(dockerCredentials);
        return request;
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources) {
        return createReserveResourcesRequest(functionResources, createDockerCredentials());
    }

    public static List<JsonObject> createResourceReservationsJson(Reservation reservation) {
        FunctionResource functionResource1 = TestObjectProvider.createFunctionResource(1L);
        FunctionResource functionResource2 = TestObjectProvider.createFunctionResource(2L);
        FunctionResource functionResource3 = TestObjectProvider.createFunctionResource(3L);

        ResourceReservation resourceReservation1 = TestObjectProvider.createResourceReservation(1L, functionResource1,
            reservation, new ResourceReservationStatus());
        ResourceReservation resourceReservation2 = TestObjectProvider.createResourceReservation(2L, functionResource2,
            reservation, new ResourceReservationStatus());
        ResourceReservation resourceReservation3 = TestObjectProvider.createResourceReservation(3L, functionResource3,
            reservation, new ResourceReservationStatus());
        return List.of(JsonObject.mapFrom(resourceReservation1), JsonObject.mapFrom(resourceReservation2),
            JsonObject.mapFrom(resourceReservation3));
    }

    public static Runtime createRuntime(long runtimeId, String name) {
        Runtime runtime = new Runtime();
        runtime.setRuntimeId(runtimeId);
        runtime.setName(name);
        runtime.setTemplatePath("");
        return runtime;
    }

    public static Runtime createRuntime(long runtimeId) {
        return createRuntime(runtimeId, "python3.9");
    }

    public static Function createFunction(long functionId, String name, String code, Runtime runtime) {
        Function function = new Function();
        function.setFunctionId(functionId);
        function.setName(name);
        function.setRuntime(runtime);
        function.setCode(code);
        return function;
    }

    public static Function createFunction(long functionId, String name, String code, long runtimeId) {
        Runtime runtime = createRuntime(runtimeId);
        return createFunction(functionId, name, code, runtime);
    }

    public static Function createFunction(long id, String name, String code) {
        Runtime runtime = createRuntime(1L);
        return createFunction(id, name, code, runtime);
    }

    public static Function createFunction(long id) {
        return createFunction(id, "foo", "false");
    }

    public static FunctionResource createFunctionResource(long id) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        Function function = createFunction(22L, "func-test", "false");
        functionResource.setFunction(function);
        Resource resource = createResource(33L);
        functionResource.setResource(resource);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, Function function, boolean isDeployed) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        functionResource.setFunction(function);
        Resource resource = createResource(33L);
        functionResource.setResource(resource);
        functionResource.setIsDeployed(isDeployed);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, Function function, Resource resource, boolean isDeployed) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        functionResource.setFunction(function);
        functionResource.setResource(resource);
        functionResource.setIsDeployed(isDeployed);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, Function function, Resource resource) {
        return createFunctionResource(id, function, resource, false);
    }

    public static Region createRegion(long id, String name) {
        return createRegion(id, name, new ResourceProvider());
    }

    public static Region createRegion(long id, String name, ResourceProvider resourceProvider) {
        Region region = new Region();
        region.setRegionId(id);
        region.setName(name);
        region.setResourceProvider(resourceProvider);
        return region;
    }

    public static VPC createVPC(long id, Region region, String vpcIdValue, String subnetIdValue, Account createdBy) {
        VPC vpc = new VPC();
        vpc.setVpcId(id);
        vpc.setRegion(region);
        vpc.setVpcIdValue(vpcIdValue);
        vpc.setSubnetIdValue(subnetIdValue);
        vpc.setCreatedBy(createdBy);
        return vpc;
    }

    public static VPC createVPC(long id, Account createdBy) {
        ResourceProvider resourceProvider = new ResourceProvider();
        resourceProvider.setProviderId(1L);
        resourceProvider.setProvider("aws");
        Region region = createRegion(1L, "us-east-1", resourceProvider);
        return createVPC(id, region, "vpc-id", "subnet-id", createdBy);
    }

    public static VPC createVPC(long id, Region region, Account createdBy) {
        return createVPC(id, region, "vpc-id", "subnet-id", createdBy);
    }

    public static ResourceReservationStatus createResourceReservationStatus(long id, ReservationStatusValue status) {
        ResourceReservationStatus rrs = new ResourceReservationStatus();
        rrs.setStatusId(id);
        rrs.setStatusValue(status.name());
        return rrs;
    }

    public static ResourceReservationStatus createResourceReservationStatusNew() {
        return createResourceReservationStatus(1L, ReservationStatusValue.NEW);
    }

    public static ResourceReservationStatus createResourceReservationStatusError() {
        return createResourceReservationStatus(2L, ReservationStatusValue.ERROR);
    }

    public static ResourceReservationStatus createResourceReservationStatusDeployed() {
        return createResourceReservationStatus(3L, ReservationStatusValue.DEPLOYED);
    }

    public static ResourceReservationStatus createResourceReservationStatusTerminating() {
        return createResourceReservationStatus(4L, ReservationStatusValue.TERMINATING);
    }

    public static ResourceReservationStatus createResourceReservationStatusTerminated() {
        return createResourceReservationStatus(5L, ReservationStatusValue.TERMINATED);
    }

    public static DockerCredentials createDockerCredentials() {
        DockerCredentials dockerCredentials = new DockerCredentials();
        dockerCredentials.setUsername("testuser");
        dockerCredentials.setAccessToken("abcdef12234");
        return dockerCredentials;
    }
}

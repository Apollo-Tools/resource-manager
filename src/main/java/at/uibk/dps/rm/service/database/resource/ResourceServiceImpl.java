package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.ResourceRepository;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceServiceImpl extends ServiceProxy<Resource> implements ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        super(resourceRepository, Resource.class);
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(resourceRepository.findByIdAndFetch(id))
            // TODO: fix
            .map(result -> {
                result.setMetricValues(null);
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<Boolean> existsOneByUrl(String url) {
        return Future
            .fromCompletionStage(resourceRepository.findByUrl(url))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsOneByResourceType(long typeId) {
        return Future
            .fromCompletionStage(resourceRepository.findByResourceType(typeId))
            .map(result -> !result.isEmpty());
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(resourceRepository.findAllAndFetch())
            .map(this::encodeResourceList);
    }


    @Override
    public Future<JsonArray> findAllByMultipleMetrics(List<String> metrics) {
        return Future
                .fromCompletionStage(resourceRepository.findByMultipleMetricsAndFetch(metrics))
                .map(this::encodeResourceList);
    }

    private JsonArray encodeResourceList(List<Resource> resourceList) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (Resource resource: resourceList) {
            for (MetricValue metricValue : resource.getMetricValues()) {
                // TODO: fix
                metricValue.setMetric(null);
                metricValue.setResource(null);
            }
            objects.add(JsonObject.mapFrom(resource));
        }
        return new JsonArray(objects);
    }
}

package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.repository.FunctionResourceRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionResourceServiceImpl extends ServiceProxy<FunctionResource> implements FunctionResourceService {
    private final FunctionResourceRepository functionResourceRepository;

    public FunctionResourceServiceImpl(FunctionResourceRepository repository) {
        super(repository, FunctionResource.class);
        functionResourceRepository = repository;
    }

    @Override
    public Future<JsonObject> findOneByFunctionAndResource(long functionId, long resourceId) {
        return Future
            .fromCompletionStage(functionResourceRepository.findByFunctionAndResource(functionId, resourceId))
            .map(functionResource -> {
                functionResource.setResource(null);
                functionResource.setFunction(null);
                return JsonObject.mapFrom(functionResource);
            });
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
            .fromCompletionStage(functionResourceRepository.findAllByReservationIdAndFetch(reservationId))
            .map(functionResources -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for ( FunctionResource functionResource: functionResources) {
                    objects.add(JsonObject.mapFrom(functionResource));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsOneByFunctionAndResource(long functionId, long resourceId) {
        return Future
            .fromCompletionStage(functionResourceRepository.findByFunctionAndResource(functionId, resourceId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> deleteByFunctionAndResource(long functionId, long resourceId) {
        return Future
            .fromCompletionStage(functionResourceRepository.deleteByFunctionAndResource(functionId, resourceId))
            .mapEmpty();
    }
}

package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.rxjava3.database.ServiceInterface;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class EntityChecker {

    private final ServiceInterface service;

    public EntityChecker(ServiceInterface service) {
        this.service = service;
    }

    public Single<JsonObject> checkFindOne(long id) {
        Single<JsonObject> findOneById = service.findOne(id);
        return ErrorHandler.handleFindOne(findOneById);
    }

    public Single<JsonArray> checkFindAll() {
        return service.findAll();
    }

    public Completable checkExistsOne(long id) {
        Single<Boolean> existsOneById = service.existsOneById(id);
        return ErrorHandler.handleExistsOne(existsOneById).ignoreElement();
    }

    public Completable checkForDuplicateEntity(JsonObject entity) {
        return Single.just(entity).ignoreElement();
    }

    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        return Single.just(entity);
    }

    public Single<JsonObject> submitCreate(JsonObject requestBody) {
        return service.save(requestBody);
    }

    public Completable submitCreateAll(JsonArray entities) {
        return service.saveAll(entities);
    }

    public Completable submitUpdate(JsonObject requestBody, JsonObject entity) {
        for (String field : requestBody.fieldNames()) {
            entity.put(field, requestBody.getValue(field));
        }
        return submitUpdate(entity);
    }

    public Completable submitUpdate(JsonObject entity) {
        return service.update(entity);
    }

    public Completable submitDelete(long id) {
        return service.delete(id);
    }
}

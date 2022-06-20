package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import javax.persistence.Persistence;
import java.util.Map;

public class DatabaseServiceImpl implements DatabaseService{
    private final SessionFactory sessionFactory;

    public DatabaseServiceImpl(JsonObject config) {
        int dbPort = config.getInteger("db_port");
        String dbHost = config.getString("db_host");
        String dbUser = config.getString("db_user");
        String dbPassword = config.getString("db_password");
        Map<String, String> props = Map.of(
                "javax.persistence.jdbc.url", "jdbc:postgresql://" + dbHost + ":" + dbPort + "/resource-manager",
                "javax.persistence.jdbc.user", dbUser,
                "javax.persistence.jdbc.password", dbPassword);

        sessionFactory = Persistence
                .createEntityManagerFactory("postgres-unit", props)
                .unwrap(SessionFactory.class);
    }

    @Override
    public Future<Void> persist(JsonObject data) {
        return null;
    }

    @Override
    public Future<JsonObject> findById(int id) {
        return null;
    }

    @Override
    public Future<JsonArray> findAll(String table) {
        return Future
                .fromCompletionStage(sessionFactory.withSession(session ->
                    session.createQuery("from " + table)
                        .getResultList()))
                .map(JsonArray::new);
    }

    @Override
    public Future<Void> update(JsonObject data) {
        return null;
    }

    @Override
    public Future<Void> remove(JsonObject data) {
        return null;
    }
}

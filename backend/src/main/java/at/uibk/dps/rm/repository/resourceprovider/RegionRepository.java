package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class RegionRepository extends Repository<Region> {

    public RegionRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Region.class);
    }

    public CompletionStage<Region> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Region r " +
                    "left join fetch r.resourceProvider " +
                    "where r.regionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }


    public CompletionStage<Region> findOneByNameAndProviderId(String name, long providerId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Region r " +
                    "left join fetch r.resourceProvider rp " +
                    "where r.name=:name and rp.providerId =:providerId", entityClass)
            .setParameter("name", name)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<Region>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Region r " +
                        "left join fetch r.resourceProvider",
                    entityClass)
                .getResultList()
        );
    }

    public CompletionStage<List<Region>> findAllByProviderId(long providerId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Region r " +
                        "where r.resourceProvider.providerId=:providerId",
                    entityClass)
                .setParameter("providerId", providerId)
                .getResultList()
        );
    }
}

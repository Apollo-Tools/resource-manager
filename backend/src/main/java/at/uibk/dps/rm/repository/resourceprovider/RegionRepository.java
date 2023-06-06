package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the region entity.
 *
 * @author matthi-g
 */
public class RegionRepository extends Repository<Region> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public RegionRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Region.class);
    }

    /**
     * Finda a region by its id and fetch the resource provider and environment.
     *
     * @param id the id of the resource provider
     * @return a CompletionStage that emits the region if it exists, else null
     */
    public CompletionStage<Region> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Region r " +
                    "left join fetch r.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "where r.regionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Fine a region by its name and resource provider.
     *
     * @param name the name of the region
     * @param providerId the id of the resource provider
     * @return a CompletionStage that emits the region if it exists, else null
     */
    public CompletionStage<Region> findOneByNameAndProviderId(String name, long providerId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Region r " +
                    "left join fetch r.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "where r.name=:name and rp.providerId =:providerId", entityClass)
            .setParameter("name", name)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all regions and fetch the resource provider and environment.
     *
     * @return a CompletionStage that emits a list of all regions
     */
    public CompletionStage<List<Region>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Region r " +
                    "left join fetch r.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "order by rp.provider", entityClass)
                .getResultList()
        );
    }

    /**
     * Find all regions by their resource provider.
     *
     * @param providerId the id of the resource provider
     * @return a CompletionStage that emits a list of all regions
     */
    public CompletionStage<List<Region>> findAllByProviderId(long providerId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Region r " +
                        "where r.resourceProvider.providerId=:providerId",
                    entityClass)
                .setParameter("providerId", providerId)
                .getResultList()
        );
    }

    /**
     * Find all regions by their supported platform.
     *
     * @param platformId the id of the platform
     * @return a CompletionStage that emits a list of all regions
     */
    public CompletionStage<List<Region>> findAllByPlatformId(long platformId) {
        return sessionFactory.withSession(session ->
            session.createQuery("SELECT reg FROM Region reg " +
                    "LEFT JOIN fetch reg.resourceProvider rp " +
                    "LEFT JOIN rp.providerPlatforms pp " +
                    "WHERE pp.platform.platformId = :platformId", entityClass)
                .setParameter("platformId", platformId)
                .getResultList()
        );
    }

    /**
     * Find region by its id and the supported platform.
     *
     * @param regionId the id of the region
     * @param platformId the id of the platform
     * @return a CompletionStage that emits a list of all regions
     */
    public CompletionStage<Region> findByRegionIdAndPlatformId(long regionId, long platformId) {
        return sessionFactory.withSession(session ->
            session.createQuery("SELECT reg FROM Region reg " +
                    "LEFT JOIN reg.resourceProvider rp " +
                    "LEFT JOIN rp.providerPlatforms pp " +
                    "WHERE reg.regionId=:regionId and pp.platform.platformId=:platformId", entityClass)
                .setParameter("regionId", regionId)
                .setParameter("platformId", platformId)
                .getSingleResultOrNull()
        );
    }
}

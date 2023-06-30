package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the vpc entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface VPCService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static VPCService create(Stage.SessionFactory sessionFactory) {
        return new VPCServiceImpl(new VPCRepository(), new RegionRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static VPCService createProxy(Vertx vertx) {
        return new VPCServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(VPC.class));
    }

    /**
     * Save a new virtual private cloud entity.
     *
     * @param accountId the id of the account
     * @param data the new entity
     * @return a Future that emits the persisted entity as JsonObject
     */
    Future<JsonObject> saveToAccount(long accountId, JsonObject data);

    /**
     * Delete a virtual private cloud entity.
     *
     * @param accountId the id of the account
     * @param vpcId the id of the vpc
     * @return an empty Future
     */
    Future<Void> deleteFromAccount(long accountId, long vpcId);

    /**
     * Find a vpc by its region and creator account.
     *
     * @param regionId the id of the region
     * @param accountId the id of the account
     * @return a Future that emits the vpc as JsonObject if it exists, else null
     */
    Future<JsonObject> findOneByRegionIdAndAccountId(long regionId, long accountId);

    /**
     * Check if a vpc exits by its region and creator account.
     *
     * @param regionId the id of the region
     * @param accountId the id of the creator account
     * @return a Future that emits true if the vpc exists, else false
     */
    Future<Boolean> existsOneByRegionIdAndAccountId(long regionId, long accountId);
}

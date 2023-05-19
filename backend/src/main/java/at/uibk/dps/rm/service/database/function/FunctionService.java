package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Set;

/**
 * The interface of the service proxy for the function entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionService create(FunctionRepository functionRepository) {
        return new FunctionServiceImpl(functionRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionService createProxy(Vertx vertx) {
        return new FunctionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Function.class));
    }

    /**
     * Check if a function exists by its name and runtime id but ignore it if the id of the
     * function is equal to the excludeId.
     *
     * @param excludeId the id to be excluded
     * @param name the name of the function
     * @param runtimeId the id of the runtime
     * @return a Future that emits true if the function exists, else false
     */
    Future<Boolean> existsOneByNameAndRuntimeIdExcludeEntity(long excludeId, String name, long runtimeId);

    /**
     * Check if function exists by its name and runtime.
     *
     * @param name the name of the function
     * @param runtimeId the id of the runtime
     * @return a Future that emits true if the function exists, else false
     */
    Future<Boolean> existsOneByNameAndRuntimeId(String name, long runtimeId);

    /**
     * Check if function exists by the functionIds.
     *
     * @param functionIds a list of functionIds
     * @return a Future that emits true if all functions exists, else false
     */
    Future<Boolean> existsAllByIds(Set<Long> functionIds);
}

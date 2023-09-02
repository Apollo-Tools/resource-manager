package at.uibk.dps.rm.rx.repository.account;

import at.uibk.dps.rm.entity.model.Role;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Implements database operations for the role entity.
 *
 * @author matthi-g
 */
public class RoleRepository extends Repository<Role> {

    /**
     * Create an instance.
     */
    public RoleRepository() {
        super(Role.class);
    }

    /**
     * Find a role by its name
     *
     * @param sessionManager the database session manager
     * @param name the name of the role
     * @return a Maybe that emits the account if it exists, else null
     */
    public Maybe<Role> findByRoleName(SessionManager sessionManager, String name) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Role r where r.role=:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull()
        );
    }
}

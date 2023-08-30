package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.Role;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

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
     * @param session the db session
     * @param name the name of the role
     * @return a CompletionStage that emits the account if it exists, else null
     */
    public CompletionStage<Role> findByRoleName(Stage.Session session, String name) {
        return session.createQuery("from Role r where r.role=:name", entityClass)
                .setParameter("name", name)
                .getSingleResultOrNull();
    }
}

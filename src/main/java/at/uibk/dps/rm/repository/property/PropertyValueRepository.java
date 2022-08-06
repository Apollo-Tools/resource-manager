package at.uibk.dps.rm.repository.property;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.property.entity.PropertyValue;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class PropertyValueRepository extends Repository<PropertyValue> {

    public PropertyValueRepository(Stage.SessionFactory sessionFactory, Class<PropertyValue> entityClass) {
        super(sessionFactory, entityClass);
    }

    public CompletionStage<PropertyValue> findByIdAndFetch(long propertyValueId) {
        return this.sessionFactory.withSession(session ->
                session.createQuery("from PropertyValue  pv left join fetch pv.property "
                                + "where pv.propertyValueId = :propertyValueId", entityClass)
                        .setParameter("propertyValueId", propertyValueId)
                        .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<PropertyValue>> findByResourceAndFetch(long resourceId) {
        return this.sessionFactory.withSession(session ->
                session.createQuery("from PropertyValue pv left join fetch pv.property "
                                + "where pv.resource.resourceId=:resourceId", entityClass)
                        .setParameter("resourceId", resourceId)
                        .getResultList()
        );
    }

    public CompletionStage<PropertyValue> findByResourceAndProperty(long resourceId, long propertyId) {
        return this.sessionFactory.withSession(session ->
                session.createQuery("from PropertyValue pv "
                                        + "where pv.resource.resourceId=:resourceId and pv.property.propertyId=:propertyId",
                                entityClass)
                        .setParameter("resourceId", resourceId)
                        .setParameter("propertyId", propertyId)
                        .getSingleResultOrNull()
        );
    }

    public CompletionStage<Integer> deleteByResourceAndProperty(long resourceId, long propertyId) {
        return this.sessionFactory.withTransaction(session ->
                session.createQuery("delete from PropertyValue pv "
                                + "where pv.resource.resourceId=:resourceId and pv.property.propertyId=:propertyId")
                        .setParameter("resourceId", resourceId)
                        .setParameter("propertyId", propertyId)
                        .executeUpdate()
        );
    }
}

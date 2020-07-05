package repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;

public class BaseRepository<T> {
    private final Class<T> clazz;
    private final static String NAME_FIELD = "name";

    public BaseRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T findById(Session session, long id) {
        return session.get(clazz, id);
    }

    public T findByName(Session session, Object value) {
        return findByField(session, NAME_FIELD, value);
    }

    public T findByField(Session session, String fieldName, Object value) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(clazz);

        Root<T> from = criteria.from(clazz);
        TypedQuery<T> typed = session.createQuery(
                criteria.select(from)
                        .where(builder.equal(from.get(fieldName), value))
        );

        return typed.getSingleResult();
    }
}

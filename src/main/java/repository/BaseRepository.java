package repository;

import java.util.List;
import java.util.Optional;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.util.CollectionUtils;

public class BaseRepository<T> {
    private final Class<T> clazz;
    private static final String NAME_FIELD = "name";

    public BaseRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T findById(Session session, long id) {
        return session.get(clazz, id);
    }

    public Optional<T> findByName(Session session, Object value) {
        return findByField(session, NAME_FIELD, value);
    }

    public Optional<T> findByField(Session session, String fieldName, Object value) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(clazz);

        Root<T> from = criteria.from(clazz);
        TypedQuery<T> typed = session.createQuery(
                criteria.select(from)
                        .where(builder.equal(from.get(fieldName), value))
        );

        List<T> list = typed.setMaxResults(1).getResultList();
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list.get(0));
    }
}

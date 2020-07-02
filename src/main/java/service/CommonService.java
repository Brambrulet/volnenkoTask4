package service;

import entity.Role;
import java.util.Objects;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;

public class CommonService {

    public static final String ADMIN_ROLE_NAME = "Admin";
    public static final String USER_ROLE_NAME = "User";

    public static <T> T findByField(String fieldName, Object value, Session session, Class<T> clazz) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(clazz);
        Root<T> from = criteria.from(clazz);
        TypedQuery<T> typed = session.createQuery(
                criteria.select(from)
                        .where(builder.equal(from.get(fieldName), value))
        );

        try {
            return typed.getSingleResult();
        } catch (final NoResultException nre) {
            return null;
        }
    }

    public static Role getAdminRole(Session session) {
        return findOrCreateRole(session, ADMIN_ROLE_NAME);
    }

    public static Role getUserRole(Session session) {
        return findOrCreateRole(session, USER_ROLE_NAME);
    }

    private static Role findOrCreateRole(Session session, String roleName) {
        Role admin = findByField("name", roleName, session, Role.class);

        if (Objects.isNull(admin)) {
            admin = new Role(roleName);
            session.save(admin);
        }

        return admin;
    }
}

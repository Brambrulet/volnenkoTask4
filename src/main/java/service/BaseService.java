package service;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.NoResultException;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.util.StringUtils;
import repository.BaseRepository;

public class BaseService<T, R extends BaseRepository<T>> {
    private static SessionFactory SESSION_FACTORY;
    private Session session;
    @Getter
    final R repository;

    private static SessionFactory getSessionFactory() {
        if (SESSION_FACTORY == null) {
            synchronized (BaseService.class) {
                if (SESSION_FACTORY == null) {
                    SESSION_FACTORY = new Configuration().configure().buildSessionFactory();
                }
            }
        }
        return SESSION_FACTORY;
    }

    BaseService(R repository) {
        this.repository = repository;
        session = getSessionFactory().openSession();
    }

    BaseService(BaseService service, R repository) {
        this.repository = repository;
        this.session = service.session;
    }

    public T findById(long id) {
        return tryExecuteQuery(session -> repository.findById(session, id));
    }

    public T findByName(String name) {
        assert !StringUtils.isEmpty(name);

        return tryExecuteQuery(session -> repository.findByName(session, name));
    }

    public T findByField(String fieldName, Object value) {
        assert !StringUtils.isEmpty(fieldName);

        return tryExecuteQuery(session -> repository.findByField(session, fieldName, value));
    }

    Session getSession() {
        return session;
    }

    T persist(T obj) {
        session.persist(obj);

        return obj;
    }

    public void execute(Consumer<Session> consumer) {
        executeQuery(s -> {consumer.accept(s); return (Void)null;});
    }

    public <X> X tryExecuteQuery(Function<Session, X> function) {
        return executeQuery(s -> {
            try {
                return function.apply(s);
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    public <X> X executeQuery(Function<Session, X> function) {
        assert !Objects.isNull(function);

        boolean localTransaction = !session.getTransaction().isActive();
        if (localTransaction) {
            session.beginTransaction();
        }
        try {
            X result = function.apply(session);
            if (localTransaction && session.isOpen() && session.getTransaction().isActive()) {
                session.getTransaction().commit();
            }

            return result;
        } catch (Exception e) {
            if (session.isOpen() && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        }
    }
}

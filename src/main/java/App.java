import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import service.CommonService;

/**
 * @author Shmelev Dmitry
 */
public class App {

    public static void main(String[] args) {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.persist(
            User.builder()
                    .role(CommonService.getAdminRole(session))
                    .name("User1")
                    .login("user1")
                    .password("1")
            .build()
        );

        session.getTransaction().commit();
        session.close();
    }
}

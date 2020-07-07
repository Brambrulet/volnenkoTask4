package repository;

import entity.Group;
import entity.User;
import java.util.List;
import org.hibernate.Session;

public class GroupRepository extends BaseRepository<Group> {
    private static final String POSTGRESQL_ADD_ALL_USERS_TO_GROUP =
            "insert into T_GROUP_USER select ?, id\n"
          + "    from T_USER\n"
          + "    where not exists(select * from T_GROUP_USER where group_id = ?);\n";

    private static final String JPQL_GET_NOT_EMPTY_GROUPS = "select group from Group group where group.users.size > 0";

    public GroupRepository() {
        super(Group.class);
    }

    public void addUserToGroup(Session session, Group group, User user) {
        session.persist(user);
        session.persist(group);

        group.getUsers().add(user);
        session.persist(group);
    }

    public void removeUserFromGroup(Session session, Group group, User user) {
        session.persist(user);
        session.persist(group);

        group.getUsers().remove(user);
        session.persist(group);
    }

    public List<Group> getNotEmptyGroups(Session session) {
        return session.createQuery(JPQL_GET_NOT_EMPTY_GROUPS, Group.class).list();
    }

    public void addAllUsersToGroup(Session session, Group group) {
        session.persist(group);

        //Session#createNativeQuery мне не понравился
        Statement.execute(session, POSTGRESQL_ADD_ALL_USERS_TO_GROUP, group.getId(), group.getId());
        session.refresh(group);
    }
}

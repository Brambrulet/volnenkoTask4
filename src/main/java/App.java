import entity.Group;
import entity.User;
import java.util.List;
import java.util.Objects;
import org.hibernate.Session;
import service.GroupService;
import service.RoleService;
import service.UserService;

/**
 * @author Shmelev Dmitry
 */
public class App {
    private static UserService userService = new UserService();
    private static GroupService groupService = new GroupService(userService);

    public static void main(String[] args) {
        User admin = userService.createAdmin("User1", "user1", "1");
        assert userService.isAdmin(admin);

        try {
            userService.createUser("User2", "user2", "");
            throw new RuntimeException("missing parameter check");
        } catch (AssertionError e) {
            //no act expected
        }

        Group group = groupService.createGroup("Total");
        groupService.addAllUsersToGroup(group);
        assert group.getUsers().size() == 1;

        List<Group> notEmptyGroups = groupService.getNotEmptyGroups();
        assert notEmptyGroups.size() == 1;

        groupService.removeUserFromGroup(group, admin);
        assert group.getUsers().size() == 0;

        groupService.execute(session -> {
            Group group2 = groupService.createGroup("Group2");

            assert !Objects.isNull(userService.createUser("User2", "user2", "2"));

            groupService.addAllUsersToGroup(group2);
            assert group2.getUsers().size() == 2;

            session.getTransaction().rollback();
        });
        assert Objects.isNull(groupService.findByName("Group2"));

        groupService.execute(Session::close);
        try {
            groupService.findByName("Group2");
            throw new RuntimeException("something wrong!");
        } catch (IllegalStateException e) {
            //no act expected
        }
    }
}

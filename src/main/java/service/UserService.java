package service;

import entity.Role;
import entity.User;
import java.util.Objects;
import org.springframework.util.StringUtils;
import repository.UserRepository;

public class UserService extends BaseService<User, UserRepository> {

    public UserService(BaseService baseService) {
        super(baseService, new UserRepository());
    }

    public UserService() {
        super(new UserRepository());
    }

    public boolean isAdmin(User user) {
        assert !Objects.isNull(user);

        return RoleService.isAdmin(user.getRole());
    }

    public User createUser(String name, String login, String passw) {
        return createUser(name, login, passw, false);
    }

    public User createAdmin(String name, String login, String passw) {
        return createUser(name, login, passw, true);
    }

    public User createUser(String name, String login, String passw, boolean admin) {
        assert !StringUtils.isEmpty(name) && !StringUtils.isEmpty(login) && !StringUtils.isEmpty(passw);

        return executeAndReturn(session -> {
            RoleService roleService = new RoleService(this);
            Role role = admin ? roleService.getAdminRole(session) : roleService.getUserRole(session);

            return persist(User.builder().name(name).login(login).password(passw).role(role).build());
        });
    }
}

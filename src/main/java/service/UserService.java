package service;

import entity.Role;
import entity.User;
import java.util.Objects;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import repository.UserRepository;

public class UserService extends BaseService<User, UserRepository> {
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    public User createUser(String name, String passw) {
        return createUser(name, passw, false);
    }

    public User createAdmin(String name, String passw) {
        return createUser(name, passw, true);
    }

    public User createUser(String name, String passw, boolean admin) {
        assert !StringUtils.isEmpty(name) && !StringUtils.isEmpty(passw);

        return executeQuery(session -> {
            RoleService roleService = new RoleService(this);
            Role role = admin ? roleService.getAdminRole(session) : roleService.getUserRole(session);

            return persist(User.builder().name(name).password(passwordEncoder.encode(passw)).role(role).build());
        });
    }

    public boolean matchPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}

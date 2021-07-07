package service;

import entity.Role;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.springframework.util.StringUtils;
import repository.BaseRepository;
import repository.RoleRepository;

public class RoleService extends BaseService<Role, RoleRepository> {

    public static final String ADMIN_ROLE_NAME = "Admin";
    public static final String USER_ROLE_NAME = "User";

    public <T, R extends BaseRepository<T>> RoleService(BaseService<T, R> baseService) {
        super(baseService, new RoleRepository());
    }

    public RoleService() {
        super(new RoleRepository());
    }

    public Role getAdminRole() {
        return findOrCreateRole(getSession(), ADMIN_ROLE_NAME);
    }

    public Role getUserRole() {
        return findOrCreateRole(getSession(), USER_ROLE_NAME);
    }

    public static boolean isAdmin(Role role) {
        assert !Objects.isNull(role);

        return ADMIN_ROLE_NAME.equals(role.getName());
    }

    private Role findOrCreateRole(Session session, String roleName) {
        assert !Objects.isNull(session) || !StringUtils.isEmpty(roleName);

        return executeQuery(s -> {
            Role admin = findByName(roleName);

            if (Objects.isNull(admin)) {
                admin = persist(new Role().setName(roleName));
            }

            return admin;
        });
    }
}

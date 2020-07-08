package service;

import entity.Group;
import entity.User;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;
import repository.GroupRepository;

public class GroupService extends BaseService<Group, GroupRepository> {

    public GroupService(BaseService baseService) {
        super(baseService, new GroupRepository());
    }

    public GroupService() {
        super(new GroupRepository());
    }

    public Group createGroup(String name) {
        assert !StringUtils.isEmpty(name);

        return persist(new Group(name));
    }

    public void addAllUsersToGroup(Group group) {
        assert !Objects.isNull(group);

        execute(session -> repository.addAllUsersToGroup(session, group));
    }

    public void addUserToGroup(Group group, User user) {
        assert !Objects.isNull(group) && !Objects.isNull(user);

        execute(session -> repository.addUserToGroup(session, group, user));
    }

    public void removeUserFromGroup(Group group, User user) {
        assert !Objects.isNull(group) && !Objects.isNull(user);

        execute(session -> repository.removeUserFromGroup(session, group, user));
    }

    public List<Group> getNotEmptyGroups() {
        List<Group> result = executeQuery(repository::getNotEmptyGroups);

        return Objects.isNull(result) ? Collections.emptyList() : result;
    }
}

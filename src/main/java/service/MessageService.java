package service;

import entity.Group;
import entity.Message;
import entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;
import pojo.MessageInfo;
import repository.BaseRepository;
import repository.MessageRepository;

public class MessageService extends BaseService<Message, MessageRepository> {

    public MessageService() {
        super(new MessageRepository());
    }

    public <T, R extends BaseRepository<T>> MessageService(BaseService<T, R> service) {
        super(service, new MessageRepository());
    }

    public Message createMessage(User sender, User recipient, Group group, String message) {
        assert !Objects.isNull(sender) && !StringUtils.isEmpty(message)
                && !(!Objects.isNull(recipient) && !Objects.isNull(group));

        return executeQuery(session -> repository.createMessage(session, sender, recipient, group, message));
    }

    public List<Message> getMessagesFor(User recipient, LocalDateTime timestamp, Group group) {
        assert !Objects.isNull(recipient) && !Objects.isNull(group);

        return executeQuery(session -> repository.getMessages(session, recipient, timestamp, group));
    }

    public List<Message> getMessagesFor(User recipient, LocalDateTime timestamp, User sender) {
        assert !Objects.isNull(recipient) && !Objects.isNull(sender);

        return executeQuery(session -> repository.getMessages(session, recipient, timestamp, sender));
    }

    public List<Message> getBroadcastMessagesFor(User recipient, LocalDateTime timestamp) {
        return executeQuery(session -> repository.getBroadcastMessagesFor(session, recipient, timestamp));
    }

    public List<MessageInfo> getMessagesInfo(User recipient, LocalDateTime timestamp) {
        assert !Objects.isNull(recipient);

        return executeQuery(session -> repository.getMessagesInfo(session, recipient, timestamp));
    }
}

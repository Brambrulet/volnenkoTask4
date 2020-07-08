package repository;

import entity.Group;
import entity.Message;
import entity.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import pojo.MessageInfo;

public class MessageRepository extends BaseRepository<Message> {
    private static final String SELECT_MESSAGES_IN_GROUP = ""
            + "select message\n"
            + "     from Message message\n"
            + "     where message.sender <> :recipient\n"
            + "         and message.group = :group\n"
            + "         and message.sended > :sended";

    private static final String SELECT_MESSAGES_FROM_SENDER = ""
            + "select message\n"
            + "     from Message message\n"
            + "     where message.recipient = :recipient\n"
            + "         and message.sender = :sender\n"
            + "         and message.sended > :sended";

    private static final String SELECT_BROADCAST_MESSAGES = ""
            + "select message\n"
            + "     from Message message\n"
            + "     where message.recipient is null\n"
            + "         and message.group is null\n"
            + "         and message.sender <> :recipient\n"
            + "         and message.sended > :sended";

    private static final String SELECT_MESSAGES_INFO = ""
            + "with params(user_id, sended) as (\n"
            + "     select ?, ?::timestamp\n"
            + ")\n"
            + "SELECT grp.name as groupName, sndr.name as senderName, count(msg.id) as quantity, max(msg.sended) as sended\n"
            + "     from t_message msg\n"
            + "     inner join params on 1 = 1\n"
            + "     left outer join t_group grp on grp.id = msg.group_id\n"
            + "     left outer join t_user sndr on sndr.id = msg.sender_id and msg.group_id is null and msg.recipient_id is not null\n"
            + "     where msg.sended > params.sended\n"
            + "         and msg.sender_id != params.user_id\n"
            + "         and (   (msg.recipient_id is null and msg.group_id is null)\n"
            + "             or  (msg.recipient_id = params.user_id and msg.group_id is null)\n"
            + "             or  (msg.recipient_id is null and exists(select group_id from t_group_user where group_id = msg.group_id and user_id = params.user_id))\n"
            + "         )\n"
            + "     group by sndr.name, grp.name\n";

    public MessageRepository() {
        super(Message.class);
    }

    public Message createMessage(Session session, User sender, User recipient, Group group, String message) {
        Message msg = Message.builder().sender(sender).recipient(recipient).group(group).message(message).build();
        session.persist(msg);

        return msg;
    }

    public List<Message> getMessages(Session session, User recipient, LocalDateTime sended, Group group) {
        return session.createQuery(SELECT_MESSAGES_IN_GROUP, Message.class)
                .setParameter("recipient", recipient)
                .setParameter("group", group)
                .setParameter("sended", sended)
                .list();
    }

    public List<Message> getMessages(Session session, User recipient, LocalDateTime sended, User sender) {
        return session.createQuery(SELECT_MESSAGES_FROM_SENDER, Message.class)
                .setParameter("recipient", recipient)
                .setParameter("sender", sender)
                .setParameter("sended", sended)
                .list();
    }

    public List<Message> getBroadcastMessagesFor(Session session, User recipient, LocalDateTime sended) {
        return session.createQuery(SELECT_BROADCAST_MESSAGES, Message.class)
                .setParameter("recipient", recipient)
                .setParameter("sended", sended)
                .list();
    }

    public List<MessageInfo> getMessagesInfo(Session session, User recipient, LocalDateTime sended) {
        //session#createNativeQuery - полный отстой
        List<MessageInfo> list = new ArrayList<>();
        Statement.executeQuery(session, SELECT_MESSAGES_INFO, resultSet -> {
            list.add(new MessageInfo(resultSet.getString(1),
                                     resultSet.getString(2),
                                     resultSet.getInt(3),
                                     resultSet.getTimestamp(4).toLocalDateTime()
                     )
            );
        }, recipient.getId(), sended.toString());
        return list;
    }
}

package server;

import entity.Group;
import entity.Message;
import entity.User;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;
import pojo.MessageInfo;
import service.GroupService;
import service.MessageService;
import service.UserService;
import utils.CommonUtils;

import static constant.CommonConst.COMMAND_INVITE;
import static constant.CommonConst.COMMAND_LIST;
import static constant.CommonConst.COMMAND_NEW;
import static constant.CommonConst.COMMAND_READ;
import static constant.CommonConst.COMMAND_SEND;
import static constant.CommonConst.KEY_COMMAND;
import static constant.CommonConst.KEY_COUNT;
import static constant.CommonConst.KEY_GROUP;
import static constant.CommonConst.KEY_IS_ADMIN;
import static constant.CommonConst.KEY_LIST;
import static constant.CommonConst.KEY_MESSAGE;
import static constant.CommonConst.KEY_MESSAGES;
import static constant.CommonConst.KEY_NAME;
import static constant.CommonConst.KEY_PASSWORD;
import static constant.CommonConst.KEY_RECIPIENT;
import static constant.CommonConst.KEY_RESULT;
import static constant.CommonConst.KEY_SENDER;
import static constant.CommonConst.KEY_TIMESTAMP;
import static constant.CommonConst.RESPONSE_ERROR;
import static constant.CommonConst.RESPONSE_OK;

public class ChatSocketListener implements Runnable {
    private Socket socket;
    private UserService userService = new UserService();
    private GroupService groupService = new GroupService(userService);
    private MessageService messageService = new MessageService(userService);

    private JSONObject json;
    private JSONObject response = new JSONObject();
    private User user;

    public ChatSocketListener(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream  reader = new DataInputStream( new BufferedInputStream( socket.getInputStream()));
             DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            try {
                json = CommonUtils.readJsonObject(reader);
                checkUser();

                if (!prepareListCommand() && !prepareReadCommand() && !prepareSendCommand()
                    && !prepareNewCommand() && !prepareInviteCommand()) {
                    throwInvalidCommand();
                }

                sendResponse(writer);
            } catch (Exception e) {
                sendError(writer, e.getMessage());
            }

            try {
                socket.close();
            } catch (IOException e) {
                //no act expected
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(DataOutputStream writer) throws IOException {
        CommonUtils.sendJsonObject(writer, response
                .put(KEY_IS_ADMIN, userService.isAdmin(user))
                .put(KEY_RESULT, RESPONSE_OK)
        );
    }

    private boolean prepareInviteCommand() {
        if (COMMAND_INVITE.equals(json.optString(KEY_COMMAND))
                && json.has(KEY_GROUP) && json.has(KEY_RECIPIENT)) {
            groupService.addUserToGroup(
                    groupService.findByName(json.getString(KEY_GROUP)).orElseThrow(() -> new RuntimeException("group not found")),
                    userService.findByName(json.getString(KEY_RECIPIENT)).orElseThrow(() -> new RuntimeException("recipient not found"))
            );

            return true;
        }

        return false;
    }

    private boolean prepareNewCommand() {
        if (COMMAND_NEW.equals(json.getString(KEY_COMMAND)) && json.has(KEY_GROUP) && userService.isAdmin(user)) {
            groupService.createGroup(json.getString(KEY_GROUP), Collections.singleton(user));

            return true;
        }

        return false;
    }

    private boolean prepareSendCommand() {
        if (COMMAND_SEND.equals(json.getString(KEY_COMMAND)) && json.has(KEY_MESSAGE)) {
            Group group = null;
            User recipient = null;

            if (existKey(KEY_GROUP)) {
                group = groupService.findByName(json.getString(KEY_GROUP)).orElseThrow(() -> new RuntimeException("group not found"));
            } else if (existKey(KEY_RECIPIENT)) {
                recipient = userService.findByName(json.getString(KEY_RECIPIENT)).orElseThrow(() -> new RuntimeException("recipient not found"));
            }

            messageService.createMessage(user, recipient, group, json.getString(KEY_MESSAGE));

            return true;
        }

        return false;
    }

    private boolean existKey(String key) {
        return json.has(key) && !StringUtils.isEmpty(json.getString(key));
    }

    private boolean prepareReadCommand() {
        if (COMMAND_READ.equals(json.getString(KEY_COMMAND)) && json.has(KEY_TIMESTAMP)) {
            List<Message> messages;
            LocalDateTime timestamp = LocalDateTime.parse(json.getString(KEY_TIMESTAMP));

            if (existKey(KEY_GROUP)) {
                messages = messageService.getMessagesFor(user, timestamp, groupService.findByName(json.getString(KEY_GROUP)).orElseThrow(() -> new RuntimeException("group not found")));
            } else if (existKey(KEY_RECIPIENT)) {
                messages = messageService.getMessagesFor(user, timestamp, userService.findByName(json.getString(KEY_RECIPIENT)).orElseThrow(() -> new RuntimeException("recipient not found")));
            } else {
                messages = messageService.getBroadcastMessagesFor(user, timestamp);
            }

            response.put(KEY_MESSAGES, new JSONArray(
                    messages.stream()
                            .map(message -> new JSONObject()
                                    .put(KEY_SENDER, message.getSenderName())
                                    .put(KEY_GROUP, message.getGroupName())
                                    .put(KEY_MESSAGE, message.getMessage())
                            )
                            .collect(Collectors.toList())
            ));
            response.put(KEY_TIMESTAMP, messages.stream().map(Message::getSended).max(LocalDateTime::compareTo).orElse(null));

            return true;
        }

        return false;
    }

    private boolean prepareListCommand() {
        if (COMMAND_LIST.equals(json.getString(KEY_COMMAND)) && json.has(KEY_TIMESTAMP)) {
            LocalDateTime timestamp = LocalDateTime.parse(json.getString(KEY_TIMESTAMP));
            List<MessageInfo> info = messageService.getMessagesInfo(user, timestamp);

            response.put(KEY_LIST, info.stream().map(messageInfo -> new JSONObject()
                        .put(KEY_GROUP, messageInfo.getGroupName())
                        .put(KEY_SENDER, messageInfo.getSenderName())
                        .put(KEY_COUNT, messageInfo.getQuantity())
                    ).collect(Collectors.toList())
            );

            return true;
        }

        return false;
    }

    private void checkUser() throws IOException {
        user = userService.findByName(json.getString(KEY_NAME)).orElseGet(this::registerNewUser);

        checkPassword();
    }

    private void checkPassword() throws IOException {
        if (!userService.matchPassword(user, json.getString(KEY_PASSWORD))) {
            throw new IOException("Invalid password");
        }
    }

    private User registerNewUser() {
        return userService.createUser(json.getString(KEY_NAME), json.getString(KEY_PASSWORD), false);
    }


    private void sendError(DataOutputStream writer, String message) throws IOException {
        CommonUtils.sendJsonObject(writer, response
                .put(KEY_MESSAGE, message)
                .put(KEY_RESULT, RESPONSE_ERROR)
        );
    }

    private void throwInvalidCommand() throws IOException {
        throw new IOException("Invalid command:" + json.toString());
    }
}

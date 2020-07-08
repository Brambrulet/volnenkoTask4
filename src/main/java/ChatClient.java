import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.json.JSONObject;
import utils.CommonUtils;

import static constant.CommonConst.COMMAND_EXIT;
import static constant.CommonConst.COMMAND_INVITE;
import static constant.CommonConst.COMMAND_LIST;
import static constant.CommonConst.COMMAND_NEW;
import static constant.CommonConst.COMMAND_READ;
import static constant.CommonConst.COMMAND_SEND;
import static constant.CommonConst.KEY_COMMAND;
import static constant.CommonConst.KEY_COUNT;
import static constant.CommonConst.KEY_GROUP;
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
import static constant.CommonConst.SERVER_PORT;

public class ChatClient {
    private static final String GROUP_PREFIX = "@";
    private static final String COMMAND_PREFIX = "#";

    private static final Scanner SCANNER = new Scanner(System.in);
    private static String input;
    private static String login;
    private static String password;
    private static boolean isAdmin = false;
    private static String recipient;
    private static String group;
    private static LocalDateTime timestamp = LocalDateTime.now();
    private static final ConcurrentLinkedQueue<JSONObject> COMMAND_QUEUE = new ConcurrentLinkedQueue<>();
    private static final CommandThread COMMAND_THREAD = new CommandThread();
    private static boolean pauseListen = false;

    public static void main(String[] args) {
        System.out.println("chat bring together");

        System.out.print("login:");
        login = SCANNER.nextLine();
        System.out.print("password:");
        password = SCANNER.nextLine();

        COMMAND_THREAD.start();

        while(true) {
            scanLine();

            if (isExitCommand()) {
                break;
            }

            processInput();
        }

        System.out.println("reality is dangerous don't do it ...");
    }

    private static void processInput() {
        if (!processList() && !processNew() && !processInvite()) {
            processSend();
        }
    }

    private static void processSend() {
        COMMAND_QUEUE.add(new JSONObject()
                .put(KEY_COMMAND, COMMAND_SEND)
                .put(KEY_MESSAGE, input)
                .put(KEY_GROUP, group)
                .put(KEY_RECIPIENT, recipient)
        );
    }

    private static boolean processInvite() {
        if (isAdmin && !Objects.isNull(group) && isInviteCommand()) {
            COMMAND_QUEUE.add(new JSONObject()
                    .put(KEY_COMMAND, COMMAND_INVITE)
                    .put(KEY_GROUP, group)
                    .put(KEY_RECIPIENT, recipient)
            );
        }
        return false;
    }

    private static boolean processNew() {
        if (isAdmin && isNewCommand()) {
            pauseListen = true;

            scanLine();
            group = input;
            recipient = null;
            COMMAND_QUEUE.add(new JSONObject()
                    .put(KEY_COMMAND, COMMAND_NEW)
                    .put(KEY_GROUP, input)
            );

            pauseListen = false;
            return true;
        }
        return false;
    }

    private static boolean processList() {
        if (isListCommand()) {
            pauseListen = true;

            COMMAND_QUEUE.add(new JSONObject()
                    .put(KEY_COMMAND, COMMAND_LIST)
            );

            scanLine();
            if (input.startsWith(GROUP_PREFIX)) {
                group = input.length() > 1 ? input.substring(1) : null;
                recipient = null;
            } else {
                group = null;
                recipient = input;
            }

            pauseListen = false;
            return true;
        }
        return false;
    }

    private static boolean isInviteCommand() {
        return isCommand(COMMAND_INVITE);
    }

    private static boolean isNewCommand() {
        return isCommand(COMMAND_NEW);
    }

    private static boolean isListCommand() {
        return isCommand(COMMAND_LIST);
    }

    private static boolean isExitCommand() {
        return isCommand(COMMAND_EXIT);
    }

    private static boolean isCommand(String command) {
        return (COMMAND_PREFIX + command).equals(input);
    }

    private static void scanLine() {
        input = SCANNER.nextLine();
    }

    static class CommandThread extends Thread {
        {setDaemon(true);}

        @Override
        public void run() {
            while(!isInterrupted()) {

                try (Socket socket = new Socket("127.0.0.1", SERVER_PORT);
                     DataInputStream reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                     DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
                    sendCommandAndProcessResponse(writer, reader);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processResponse(JSONObject response) {
            if (isError(response)) {
                System.out.println("ERROR: " + response.optString(KEY_MESSAGE));
            } else if (RESPONSE_OK.equals(response.getString(KEY_RESULT))) {
                printMessages(response);
                printList(response);
            }
        }

        private boolean isError(JSONObject response) {
            return RESPONSE_ERROR.equals(response.getString(KEY_RESULT));
        }

        private void printMessages(JSONObject response) {
            if (response.has(KEY_MESSAGES) && !response.getJSONArray(KEY_MESSAGES).isEmpty()) {
                timestamp = LocalDateTime.parse(response.getString(KEY_TIMESTAMP));

                response.getJSONArray(KEY_MESSAGES).forEach(msg -> {
                    JSONObject message = (JSONObject) msg;
                    System.out.println(
                            message.optString(KEY_SENDER) + message.optString(KEY_GROUP)
                                    + ": " + message.optString(KEY_MESSAGE)
                    );
                });
            }
        }

        private void printList(JSONObject response) {
            if (response.has(KEY_LIST) && !response.getJSONArray(KEY_LIST).isEmpty()) {

                response.getJSONArray(KEY_LIST).forEach(msg -> {
                    JSONObject message = (JSONObject) msg;
                    System.out.println(
                            message.optString(KEY_SENDER, GROUP_PREFIX) + message.optString(KEY_GROUP)
                                    + ": " + message.getInt(KEY_COUNT) + " messages"
                    );
                });
            }
        }

        private void sendCommandAndProcessResponse(DataOutputStream writer, DataInputStream reader) throws IOException {
            JSONObject command = COMMAND_QUEUE.poll();
            if (Objects.isNull(command)) {
                if (pauseListen) {
                    return;
                }
                command = createReadCommand();
            }

            CommonUtils.sendJsonObject(writer, command
                    .put(KEY_TIMESTAMP, timestamp)
                    .put(KEY_NAME, login)
                    .put(KEY_PASSWORD, password)
            );

            processResponse(CommonUtils.readJsonObject(reader));
        }

        private JSONObject createReadCommand() {
            return new JSONObject()
                    .put(KEY_COMMAND, COMMAND_READ)
                    .put(KEY_GROUP, group)
                    .put(KEY_RECIPIENT, recipient)
                    .put(KEY_TIMESTAMP, timestamp);
        }
    }
}

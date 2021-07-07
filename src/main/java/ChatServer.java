import entity.Settings;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.ChatSocketListener;
import service.SettingsService;

import static constant.CommonConst.SERVER_PORT;

/**
 * @author Shmelev Dmitry
 */
public class ChatServer {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new SettingsService().getSettings().forEach(Settings::print);

        try(ServerSocket socket = new ServerSocket(SERVER_PORT)) {
            System.out.println("server started");

            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                executor.submit(new ChatSocketListener(socket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
    }
}

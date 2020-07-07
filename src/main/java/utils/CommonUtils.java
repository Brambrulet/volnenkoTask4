package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class CommonUtils {

    public static JSONObject readJsonObject(DataInputStream reader) throws IOException {
        byte[] bytes = new byte[Integer.parseInt(reader.readLine())];
        reader.readFully(bytes);

        return new JSONObject(new String(bytes, StandardCharsets.UTF_8));
    }

    public static void sendJsonObject(DataOutputStream writer, JSONObject json) throws IOException {
        byte[] toResponse = json.toString().getBytes(StandardCharsets.UTF_8);
        writer.writeBytes(toResponse.length + "\n");
        writer.write(toResponse);
        writer.flush();
    }

}

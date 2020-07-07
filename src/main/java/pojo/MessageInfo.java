package pojo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo {

    String groupName;

    String senderName;

    int quantity;

    LocalDateTime sended;
}

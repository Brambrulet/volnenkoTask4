package entity;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "T_MESSAGE")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message extends IndexedComparableEntity {

    @Column(updatable = false, nullable = false)
    private LocalDateTime sended = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(optional = true)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne(optional = true)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(updatable = false, nullable = false)
    private String message;

    public String getSenderName() {
        return Objects.isNull(sender) ? null : sender.getName();
    }

    public String getGroupName() {
        return Objects.isNull(group) ? null : group.getName();
    }
}

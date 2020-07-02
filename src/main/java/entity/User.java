package entity;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "T_USER")
@Data
@Builder
public class User implements Comparable<User>{
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column(unique = true)
    private String login;

    @Column
    private String password;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToMany(mappedBy = "users")
    private Set<Group> groups;

    @Override
    public int compareTo(User other) {
        long result = id - other.id;
        return result < Integer.MAX_VALUE && result > Integer.MIN_VALUE ? (int)result : (int)(result >> 32);
    }
}

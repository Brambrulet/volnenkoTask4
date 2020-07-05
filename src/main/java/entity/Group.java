package entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GROUP")
@Data
@NoArgsConstructor
public class Group implements Comparable<Group>{
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "T_GROUP_USER",
            joinColumns = @JoinColumn(name = "group_id") ,
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    public Group(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Group other) {
        long result = id - other.id;
        return result < Integer.MAX_VALUE && result > Integer.MIN_VALUE ? (int)result : (int)(result >> 32);
    }
}

package entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "T_GROUP")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Group extends IndexedComparableEntity {

    @Column(unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "T_GROUP_USER",
            joinColumns = @JoinColumn(name = "group_id") ,
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;
}

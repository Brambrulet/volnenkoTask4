package entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity(name = "T_SETTINGS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorColumn(name = "param_name", discriminatorType = DiscriminatorType.STRING)
@IdClass(Settings.SettingsPK.class)
public abstract class Settings implements Serializable {
    @Id
    @Column(name = "param_name", nullable = false, insertable = false, updatable = false)
    protected String paramName;

    @Id
    @Column(name = "sub_name", nullable = true)
    protected String subName;

    public abstract void print();

    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Data
    public static class SettingsPK implements Serializable {
        private String paramName;
        private String subName;
    }
}

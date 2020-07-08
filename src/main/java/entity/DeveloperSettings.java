package entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;

@Entity
@NoArgsConstructor
@DiscriminatorValue(DeveloperSettings.TYPE_VALUE)
@Data
@Accessors(chain = true)
public class DeveloperSettings extends Settings {
    public static final String TYPE_VALUE = "development.author";

    @Column(name = "param_value")
    private String fio;

    @Column(name = "param_value2")
    private String email;

    @Override
    public void print() {
        System.out.println("author: " + fio + " " + email);
    }

    public DeveloperSettings(String subname) {
        super(TYPE_VALUE, subname);
    }
}

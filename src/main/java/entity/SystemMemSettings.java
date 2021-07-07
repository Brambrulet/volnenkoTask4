package entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@DiscriminatorValue(SystemMemSettings.TYPE_VALUE)
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class SystemMemSettings extends Settings {
    public static final String TYPE_VALUE = "system.requirements.minMemory";

    @Column(name = "param_value")
    private String paramValue;

    @Override
    public void print() {
        System.out.println(paramName + ": " + paramValue);
    }

    public SystemMemSettings(String subname) {
        super(TYPE_VALUE, subname);
    }
}

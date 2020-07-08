package entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@DiscriminatorValue(SystemOsSettings.TYPE_VALUE)
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class SystemOsSettings extends Settings {
    public static final String TYPE_VALUE = "system.requirements.minOsVer";

    @Column(name = "param_value")
    private String paramValue;

    @Override
    public void print() {
        System.out.println(paramName + ": " + paramValue);
    }

    public SystemOsSettings(String subname) {
        super(TYPE_VALUE, subname);
    }
}

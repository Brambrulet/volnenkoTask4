package service;

import entity.DeveloperSettings;
import entity.Settings;
import entity.SystemMemSettings;
import entity.SystemOsSettings;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.springframework.util.CollectionUtils;
import repository.BaseRepository;
import repository.SettingsRepository;

public class SettingsService extends BaseService<Settings, SettingsRepository>  {

    public SettingsService() {
        super(new SettingsRepository());
    }

    public <T, R extends BaseRepository<T>> SettingsService(BaseService<T, R> service) {
        super(service, new SettingsRepository());
    }

    public List<Settings> getSettings() {
        List<Settings> settings = new ArrayList<>();

        settings.addAll(getDeveloperSettings());
        settings.add(getOsRequirements());
        settings.add(getMemRequirements());

        return settings;
    }

    private Settings getMemRequirements() {
        return executeQuery(session -> {
            List<Settings> settings =
                    session.createQuery("select systemMemSettings from SystemMemSettings systemMemSettings order by subName desc",
                                        Settings.class)
                            .setMaxResults(1)
                            .list();

            return CollectionUtils.isEmpty(settings)
                    ? persist(new SystemMemSettings("#1").setParamValue("1024Mb"))
                    : settings.get(0);
        });
    }

    private Settings getOsRequirements() {
        return executeQuery(session -> {
            String osType = System.getProperty("os.name").toLowerCase().split("\\s")[0];
            List<Settings> settings =
                    session.createQuery("select s from SystemOsSettings s where s.subName like concat(:osType, '%')",
                                        Settings.class)
                            .setParameter("osType", osType)
                            .setMaxResults(1)
                            .list();

            return CollectionUtils.isEmpty(settings)
                    ? persist(new SystemOsSettings(osType).setParamValue("XP"))
                    : settings.get(0);
        });
    }

    public List<Settings> getDeveloperSettings() {
        return executeQuery(session -> {
            List<Settings> settings = session.createQuery("select s from DeveloperSettings s", Settings.class).list();

            return CollectionUtils.isEmpty(settings)
                    ? Collections.singletonList(persist(new DeveloperSettings("#1")
                                                                .setFio("Dmitry Shmelev")
                                                                .setEmail("brambrulet@gmail.com")
                                                                ))
                    : settings;
        });
    }
}

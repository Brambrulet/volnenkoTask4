package repository;

import entity.Role;
import entity.Settings;

public class SettingsRepository extends BaseRepository<Settings> {

    public SettingsRepository() {
        super(Settings.class);
    }
}

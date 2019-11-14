package me.bristermitten.privatemines.config.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MenuConfig {

    private Map<String, ConfigurationSection> menuConfigs = new HashMap<>();

    public MenuConfig(FileConfiguration configuration) {
        load(configuration);
    }

    public void load(FileConfiguration config) {
        for (String menus : config.getConfigurationSection("Menus").getKeys(false)) {
            menuConfigs.put(menus, config.getConfigurationSection("Menus." + menus));
        }
    }

    public ConfigurationSection forName(String name) {
        return menuConfigs.getOrDefault(name, null);
    }
}

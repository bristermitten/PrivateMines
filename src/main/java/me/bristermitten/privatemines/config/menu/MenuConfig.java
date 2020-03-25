package me.bristermitten.privatemines.config.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MenuConfig {

    private final Map<String, ConfigurationSection> menuConfigs = new HashMap<>();

    public MenuConfig(FileConfiguration configuration) {
        load(configuration);
    }

    /*
     Loads the config files for the menus.
     */
    public void load(FileConfiguration config) {
        for (String menus : config.getConfigurationSection("Menus").getKeys(false)) {
            menuConfigs.put(menus, config.getConfigurationSection("Menus." + menus));
        }
    }

    /*
       Unknown, please comment what this part does.
     */
    public ConfigurationSection configurationForName(String name) {
        return menuConfigs.getOrDefault(name, null);
    }
}

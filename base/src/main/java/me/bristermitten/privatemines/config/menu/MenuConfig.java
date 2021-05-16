package me.bristermitten.privatemines.config.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MenuConfig {

    private final Map<String, ConfigurationSection> menuConfigs = new HashMap<>();

    public MenuConfig(FileConfiguration configuration) {
        load(configuration);
    }

    /**
     Loads the config files for the menus.
     */
    public void load(FileConfiguration config) {
        if (config.getConfigurationSection("Menus") == null) {
            Bukkit.getLogger().warning("The configuration section for menus was missing!");
        } else {
            for (String menus : Objects.requireNonNull(config.getConfigurationSection("Menus")).getKeys(false)) {
                menuConfigs.put(menus, config.getConfigurationSection("Menus." + menus));
            }
        }
    }

    /**
     * Gets a configuration for a given menu config
     */
    public ConfigurationSection configurationForName(String name) {
        return menuConfigs.getOrDefault(name, null);
    }
}

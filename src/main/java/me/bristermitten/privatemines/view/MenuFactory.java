package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;

/**
 * Factory pattern for creating Menus
 */
public class MenuFactory {
    private final MineStorage storage;

    private final PrivateMines plugin;
    private final MenuConfig config;
    private final PMConfig pmConfig;

    public MenuFactory(MineStorage storage, PrivateMines plugin, MenuConfig config, PMConfig pmConfig) {
        this.storage = storage;
        this.plugin = plugin;
        this.config = config;
        this.pmConfig = pmConfig;
    }

    public void createMainMenu(Player p) {
        new PrivateMineMenu(p, plugin, config, storage, pmConfig);
    }

    public void createAndOpenMinesMenu(Player p) {
        new MinesMenu(config, plugin, storage).open(p);
    }
}

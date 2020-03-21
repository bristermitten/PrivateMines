package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Factory pattern for creating Menus
 */
public class MenuFactory {
    private final MineStorage storage;

    private final PrivateMines plugin;
    private final MenuConfig config;
    private final PMConfig pmConfig;

    private final File file;

    public MenuFactory(MineStorage storage, PrivateMines plugin, MenuConfig config, PMConfig pmConfig, File file) {
        this.storage = storage;
        this.plugin = plugin;
        this.config = config;
        this.pmConfig = pmConfig;
        this.file = file;
    }

    public void createMainMenu(Player p) {
        new PrivateMineMenu(p, plugin, config, storage, pmConfig, file);
    }

    public void createAndOpenMinesMenu(Player p) {
        new MinesMenu(config, plugin, storage, file).open(p);
    }
}

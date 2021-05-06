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

	public void openMainMenu(Player p) {
		new PrivateMineMenu(p, plugin, config, storage, pmConfig, this);
	}

	public void createAndOpenMinesMenu(Player p) {
		new MinesMenu(p, config, plugin, storage);
	}

	public void createAndOpenThemeMenu(Player p) {
		new ChangeThemeMenu(p, plugin, config, storage);
	}

	public void createAndOpenResetPercentMenu(Player p) {
		new ChangeMineResetPercentMenu(p, plugin, pmConfig, config, storage);
	}

}

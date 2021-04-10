package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PrivateMineMenu {

	private static MenuSpec original;

	/*
	  Manages the /pmine Menu
	 */
	public PrivateMineMenu(Player p, PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig,
	                       MenuFactory factory) {
		Validate.notNull(p, "Player");
		Validate.notNull(plugin, "PrivateMines");
		Validate.notNull(config, "MenuConfig");
		Validate.notNull(storage, "MineStorage");
		Validate.notNull(pmConfig, "PMConfig");

		if (original == null) {
			original = new MenuSpec();

			original.addAction("go-to-mine",
					e -> goToMine(storage, e));
			original.addAction("view-mines",
					e -> openMinesMenu(plugin, config, storage, e));
			original.addAction("change-block",
					e -> openChangeBlockMenu(plugin, config, storage, pmConfig, e));
			original.addAction("change-schematic",
					e -> factory.createAndOpenThemeMenu((Player) e.getWhoClicked()));
			original.addAction("change-reset-effect",
					e -> openChangeEffectsMenu(plugin, config, storage, pmConfig, e));
			original.addAction("change-reset-style",
					e -> openResetStyleMenu(plugin, config, storage, pmConfig, e));
		}

		PrivateMine mine = storage.get(p);
		Material type = mine != null ? Material.valueOf(String.valueOf(mine.getMineBlocks().get(0))) : Material.STONE;

		original.loadFrom(config.configurationForName("Main"), "%BLOCK%", type);

		MenuSpec menuSpec = new MenuSpec();

		menuSpec.copyFrom(original);
		menuSpec.register(plugin);

		p.openInventory(menuSpec.genMenu());
	}

	/*
	  Create the click event for going to the mine.
	 */
	private void goToMine(MineStorage storage, InventoryClickEvent e) {
		storage.getOrCreate((Player) e.getWhoClicked()).teleport();
	}

	private void openMinesMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, InventoryClickEvent e) {
		new MinesMenu((Player) e.getWhoClicked(), config, plugin, storage);
	}

	private void openChangeBlockMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig,
	                                 InventoryClickEvent e) {
		new ChangeBlockMenu((Player) e.getWhoClicked(), plugin, pmConfig, config, storage);
	}

	private void openResetStyleMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig,
									   InventoryClickEvent e) {
		new ChangeResetStyleMenu((Player) e.getWhoClicked(), plugin, pmConfig, config, storage);
	}

	private void openChangeEffectsMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig,
									 InventoryClickEvent e) {
		new ChangeEffectsMenu((Player) e.getWhoClicked(), plugin, pmConfig, config, storage);
	}
}

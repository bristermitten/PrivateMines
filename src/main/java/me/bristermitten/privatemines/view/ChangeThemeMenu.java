package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.service.SchematicStorage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles the menu to change a Private Mine theme
 */
public class ChangeThemeMenu {

	private static MenuSpec original;

	public ChangeThemeMenu(Player p, PrivateMines plugin, MenuConfig menuConfig, MineStorage storage) {
		if (original == null) {
			original = new MenuSpec();
			original.loadFrom(menuConfig.configurationForName("Schematics"));
		}

		MenuSpec spec = new MenuSpec();
		spec.copyFrom(original);
		spec.register(plugin);

		PrivateMine mine = storage.getOrCreate(p);
		MineSchematic[] schematics = SchematicStorage.getInstance().getAll().toArray(new MineSchematic[0]);

		p.openInventory(spec.genMenu((schematic, i) -> {
					i.setType(schematic.getIcon().getType());

					ItemMeta itemMeta = i.getItemMeta();
					String displayName = itemMeta.getDisplayName();
					String name = displayName.replace("%name%", Util.prettify(schematic.getName()));
					itemMeta.setDisplayName(name);
					itemMeta.setLore(Util.color(schematic.getDescription()));
					i.setItemMeta(itemMeta);
					return i;
				},
				schematic -> e -> {
					mine.setMineSchematic(schematic);
					mine.teleport((Player) e.getWhoClicked());
				},
				schematics));
	}
}

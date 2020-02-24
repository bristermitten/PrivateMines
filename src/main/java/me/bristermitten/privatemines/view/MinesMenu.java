package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

import static me.bristermitten.privatemines.Util.prettify;

/**
 * Menu for viewing all open Private Mines
 */
public class MinesMenu {
    private static MenuSpec original;

    private final Inventory inventory;

    public MinesMenu(MenuConfig config, PrivateMines plugin, MineStorage storage, File f) {

        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(config.configurationForName("Mines"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);

        Player[] players =
                Bukkit.getOnlinePlayers().stream().filter(storage::has)
                        .filter(pl -> storage.get(pl).isOpen())
                        .toArray(Player[]::new);

        inventory = spec.genMenu(
                (pl, i) -> {
                    ItemMeta itemMeta = i.getItemMeta();
                    PrivateMine mine = storage.getOrCreate(pl, f);
                    Util.replaceMeta(itemMeta,
                            "%player%", pl.getName(),
                            "%block%", prettify(mine.getBlock().name()),
                            "%tax%", mine.getTaxPercentage());
                    i.setItemMeta(itemMeta);
                    return i;
                },
                pl -> e -> storage.getOrCreate(pl, f).teleport((Player) e.getWhoClicked()), players);

        spec.register(plugin);
    }

    public void open(Player p) {
        p.openInventory(inventory);
    }
}

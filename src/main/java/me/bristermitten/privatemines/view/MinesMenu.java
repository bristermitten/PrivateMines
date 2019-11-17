package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import static me.bristermitten.privatemines.Util.prettify;

public class MinesMenu {
    private static MenuSpec original;

    public MinesMenu(Player p, MenuConfig config, PrivateMines plugin, MineStorage storage) {

        if (original == null) {
            original = new MenuSpec();
            original.load(config.forName("Mines"));
        }

        MenuSpec spec = new MenuSpec();
        spec.load(original);
        spec.register(plugin);
        Player[] players =
                Bukkit.getOnlinePlayers().stream().filter(storage::has).filter(pl -> storage.get(pl).isOpen())
                        .toArray(Player[]::new);
        Inventory inventory = spec.genMenu(
                (pl, i) -> {
                    ItemMeta itemMeta = i.getItemMeta();
                    Util.replaceMeta(itemMeta,
                            "%player%", pl.getName(),
                            "%block%", prettify(storage.getOrCreate(pl).getBlock().name()),
                            "%tax%", String.valueOf(storage.getOrCreate(pl).getTaxPercentage()));
                    i.setItemMeta(itemMeta);
                    return i;
                },
                pl -> e -> storage.getOrCreate(pl).teleport((Player) e.getWhoClicked()), players);
        spec.register(plugin);
        p.openInventory(inventory);
    }
}

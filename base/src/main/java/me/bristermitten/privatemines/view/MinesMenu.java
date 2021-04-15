package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Menu for viewing all open Private Mines
 */
public class MinesMenu
{

    private static MenuSpec original;

    MinesMenu(Player p, MenuConfig config, PrivateMines plugin, MineStorage storage)
    {

        if (original == null)
        {
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

        /*
                            String name = displayName.replace("%block%",
                            Util.getName(block).orElse("Unknown"));
         */
        p.openInventory(spec.genMenu(
                (pl, i) -> {
                    ItemMeta itemMeta = i.getItemMeta();
                    PrivateMine mine = storage.getOrCreate(pl);
                    Util.replaceMeta(itemMeta,
                            "%player%", pl.getName(),
//                            "%blocks%", prettify(mine.getBlocks().toString()),
                            "%tax%", plugin.isAutoSellEnabled() ? mine.getTaxPercentage() : "Tax Disabled.",
                            "%tier%", mine.getMineTier());
                    i.setItemMeta(itemMeta);
                    return i;
                },
                pl -> e -> storage.getOrCreate(pl).teleport((Player) e.getWhoClicked()), players));
    }
}

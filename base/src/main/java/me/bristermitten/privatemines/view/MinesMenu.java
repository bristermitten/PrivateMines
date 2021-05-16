package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.util.Formatting;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

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

        if (storage != null) {
            Player[] players =
                    Bukkit.getOnlinePlayers().stream().filter(storage::hasMine)
                            .filter(pl -> storage.get(pl).isOpen())
                            .toArray(Player[]::new);

            p.openInventory(spec.genMenu(
                    (pl, i) -> {
                        final ItemMeta itemMeta = i.getItemMeta();
                        Objects.requireNonNull(itemMeta);
                        final PrivateMine mine = storage.get(pl);
                        final List<String> formattedBlocks = Formatting.getMineBlocksFormatted(mine.getMineBlocks());

                        Util.replaceMeta(itemMeta,
                                "%player%", pl.getName(),
                                "%block%", Util.prettify(mine.getMineBlocks().get(0).getType().toString()),
                                "%blocks%", mine.getMineBlocks(),
                                "%blocksFormatted%", formattedBlocks,
                                "%tax%", plugin.isAutoSellEnabled() ? mine.getTaxPercentage() : "Tax Disabled.");

                        i.setItemMeta(itemMeta);
                        return i;
                    },
                    pl -> e -> storage.get(pl).teleport((Player) e.getWhoClicked()), players));
        }
    }
}

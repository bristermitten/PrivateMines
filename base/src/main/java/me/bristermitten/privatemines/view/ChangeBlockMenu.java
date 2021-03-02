package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the menu to change a Private Mine block
 */
public class ChangeBlockMenu {

    private static MenuSpec original;

    /*
    Loads the configuration for the change block menu
    TODO Check
     */
    public ChangeBlockMenu(Player p, PrivateMines plugin, PMConfig config, MenuConfig menuConfig, MineStorage storage) {
        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(menuConfig.configurationForName("Blocks"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);
        PrivateMine mine = storage.getOrCreate(p);
        ItemStack[] blocks = config.getBlockOptions().toArray(new ItemStack[]{});
        p.openInventory(spec.genMenu((block, i) -> {
                    i.setType(block.getType());
                    i.setDurability(block.getDurability());
                    ItemMeta itemMeta = i.getItemMeta();
                    String displayName = itemMeta.getDisplayName();
                    String name = displayName.replace("%block%",
                            Util.getName(block).orElse("Unknown"));
                    itemMeta.setDisplayName(name);
                    List<String> lore = itemMeta.getLore().stream().map(s -> s.replace("%block%",
                            Util.getName(block).orElse("Unknown"))).collect(Collectors.toList());
                    itemMeta.setLore(lore);
                    i.setItemMeta(itemMeta);
                    return i;
                },
                block -> e -> {
                    if (p.hasPermission("privatemine.block." + block.getType().name())) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                        mine.setBlock(block);
                        }, 20L);

                    } else {
                        p.sendMessage(ChatColor.RED + "No access to this block!");
                    }
                }, blocks));
    }
}
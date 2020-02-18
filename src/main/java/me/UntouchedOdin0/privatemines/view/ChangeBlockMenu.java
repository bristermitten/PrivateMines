package me.UntouchedOdin0.privatemines.view;

import me.UntouchedOdin0.privatemines.PrivateMines;
import me.UntouchedOdin0.privatemines.Util;
import me.UntouchedOdin0.privatemines.config.PMConfig;
import me.UntouchedOdin0.privatemines.config.menu.MenuConfig;
import me.UntouchedOdin0.privatemines.config.menu.MenuSpec;

import me.UntouchedOdin0.privatemines.data.PrivateMine;
import me.UntouchedOdin0.privatemines.service.MineStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ChangeBlockMenu {

    private static MenuSpec original;

    public ChangeBlockMenu(Player p, PrivateMines plugin, PMConfig config, MenuConfig menuConfig, MineStorage storage) {
        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(menuConfig.configurationForName("Blocks"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);
        PrivateMine mine = storage.getOrCreate(p);
        Material[] blocks = config.getBlockOptions().toArray(new Material[]{});
        p.openInventory(spec.genMenu((block, i) -> {
                    i.setType(block);
                    ItemMeta itemMeta = i.getItemMeta();
                    String displayName = itemMeta.getDisplayName();
                    String name = displayName.replace("%block%", Util.prettify(block.name()));
                    itemMeta.setDisplayName(name);
                    List<String> lore = itemMeta.getLore().stream().map(s -> s.replace("%block%",
                            Util.prettify(block.name()))).collect(Collectors.toList());
                    itemMeta.setLore(lore);
                    i.setItemMeta(itemMeta);
                    return i;
                },
                block -> e -> mine.setBlock(block), blocks));
    }
}

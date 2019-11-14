package me.bristermitten.privatemines.config.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MenuSpec {

    private Map<Integer, ItemStack> itemsMap = new HashMap<>();

    private Map<Integer, Consumer<InventoryClickEvent>> actionSlotMap = new HashMap<>();
    private Map<String, Consumer<InventoryClickEvent>> actionMap = new HashMap<>();
    private int size;

    private String title;
    private Inventory inventory;

    public MenuSpec putAction(String name, Consumer<InventoryClickEvent> action) {
        actionMap.put(name, action);
        return this;
    }

    public void load(ConfigurationSection section) {
        title = ChatColor.translateAlternateColorCodes('&', section.getString("Title"));
        size = section.getInt("Size");
        ConfigurationSection items = section.getConfigurationSection("Items");
        for (String key : items.getKeys(false)) {
            ConfigurationSection s = items.getConfigurationSection(key);
            int slot = s.getInt("Slot", Integer.parseInt(s.getName()));
            itemsMap.put(slot, ItemStack.deserialize(s.getValues(true)));
            if (s.contains("Action")) {
                actionSlotMap.put(slot, actionMap.get(s.getString("Action")));
            }
        }
    }

    public Inventory genMenu() {
        this.inventory = Bukkit.createInventory(null, size, title);
        itemsMap.forEach(inventory::setItem);

        return inventory;
    }

    public Inventory getMenu() {
        return inventory;
    }


    public Listener toListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!e.getInventory().equals(inventory)) return;
                if (!actionSlotMap.containsKey(e.getSlot())) return;
                actionSlotMap.get(e.getSlot()).accept(e);
            }
        };
    }

    public void register(Plugin p) {
        Bukkit.getPluginManager().registerEvents(toListener(), p);
    }
}

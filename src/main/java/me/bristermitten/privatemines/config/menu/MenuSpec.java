package me.bristermitten.privatemines.config.menu;

import me.bristermitten.privatemines.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class MenuSpec {

    private final InventoryHolder holder;
    private Map<Integer, ItemStack> itemsMap = new HashMap<>();
    private Map<Integer, Consumer<InventoryClickEvent>> actionSlotMap = new HashMap<>();
    private Map<String, Consumer<InventoryClickEvent>> actionMap = new HashMap<>();
    private Map<String, Predicate<InventoryClickEvent>> conditionMap = new HashMap<>();
    private Map<Integer, Predicate<InventoryClickEvent>> conditionSlotMap = new HashMap<>();
    private int size;
    private String title;
    private Inventory inventory;
    private Listener l;
    private ItemStack backgroundItem;

    private ItemStack everyItem;

    public MenuSpec() {
        holder = new MenuHolder();
    }

    public MenuSpec putAction(String name, Consumer<InventoryClickEvent> action) {
        actionMap.put(name, action);
        return this;
    }

    public MenuSpec putCondition(String name, Predicate<InventoryClickEvent> action) {
        conditionMap.put(name, action);
        return this;
    }

    public void load(MenuSpec other) {
        this.title = other.title;
        this.size = other.size;
        this.itemsMap = other.itemsMap;
        this.actionSlotMap = other.actionSlotMap;
        this.actionMap = other.actionMap;
        this.conditionMap = other.conditionMap;
        this.conditionSlotMap = other.conditionSlotMap;
        this.backgroundItem = other.backgroundItem;
        this.everyItem = other.everyItem;
    }

    public void load(ConfigurationSection section, String... placeholders) {
        title = Util.color(section.getString("Title"));
        size = section.getInt("Size");
        ConfigurationSection items = section.getConfigurationSection("Items");
        if (items.contains("Every")) {
            everyItem = Util.deserializeStack(items.getConfigurationSection("Every").getValues(true));
        } else for (String key : items.getKeys(false)) {

            ConfigurationSection s = items.getConfigurationSection(key);

            int slot = s.getInt("Slot", Integer.parseInt(s.getName()));
            ItemStack item = Util.deserializeStack(s.getValues(true), placeholders);
            itemsMap.put(slot, item);
            if (s.contains("Action")) {
                actionSlotMap.put(slot, actionMap.get(s.getString("Action")));
            }
            if (s.contains("Condition")) {
                conditionSlotMap.put(slot, conditionMap.get(s.getString("Condition")));
            }
        }
        if (section.contains("Background")) {
            backgroundItem = Util.deserializeStack(section.getConfigurationSection("Background").getValues(true));
        }
    }

    public Inventory genMenu() {
        this.inventory = Bukkit.createInventory(holder, size, title);
        itemsMap.forEach(inventory::setItem);

        if (backgroundItem != null)
            IntStream.range(0, size).filter(i -> !itemsMap.containsKey(i))
                    .forEach(i -> inventory.setItem(i, backgroundItem));
        return inventory;
    }

    @SafeVarargs
    public final <T> Inventory genMenu(BiFunction<T, ItemStack, ItemStack> toItem,
                                       Function<T, Consumer<InventoryClickEvent>> clickFunction, T... ts) {
        this.inventory = Bukkit.createInventory(holder, size, title);
        for (int i = 0, tsLength = ts.length; i < tsLength; i++) {
            T t = ts[i];
            ItemStack newItem = toItem.apply(t, everyItem.clone());
            inventory.addItem(newItem);
            actionSlotMap.put(i, clickFunction.apply(t));
        }
        IntStream.range(0, size)
                .filter(i -> !actionSlotMap.containsKey(i))
                .forEach(i -> inventory.setItem(i, backgroundItem));
        return inventory;
    }

    public Inventory getMenu() {
        return inventory;
    }

    public Listener toListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!e.getInventory().getHolder().equals(holder)) return;
                e.setCancelled(true);
                if (!actionSlotMap.containsKey(e.getSlot())) return;
                actionSlotMap.get(e.getSlot()).accept(e);
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (!e.getInventory().getHolder().equals(holder)) return;
                InventoryClickEvent.getHandlerList().unregister(l);
                e.getHandlers().unregister(l);

                //dereference to allow gc
                l = null;
                actionMap.clear();
                actionSlotMap.clear();
                conditionMap.clear();
                conditionSlotMap.clear();
                itemsMap.clear();
                inventory = null;
                backgroundItem = null;
                everyItem = null;
                title = null;
            }
        };
    }

    public void register(Plugin p) {
        if (l != null) return;
        l = toListener();
        Bukkit.getPluginManager().registerEvents(l, p);
    }

    private class MenuHolder implements InventoryHolder {
        private final UUID uuid = UUID.randomUUID();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MenuHolder)) return false;
            MenuHolder that = (MenuHolder) o;
            return Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}

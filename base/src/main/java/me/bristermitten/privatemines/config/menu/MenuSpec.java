package me.bristermitten.privatemines.config.menu;

import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.view.MinesMenu;
import me.bristermitten.privatemines.view.PrivateMineMenu;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A specification for a Menu. This is sort of like a template,
 * and can be copied for placeholders in each item.
 * <p>
 * The general pattern is that 1 MenuSpec holds the actual "raw" data from the config,
 * and the rest are copied and have placeholders applied.
 * <p>
 * For an example see {@link PrivateMineMenu}
 */
public class MenuSpec {

    private final InventoryHolder holder;
    /**
     * Maps slot to Item
     */
    private Map<Integer, ItemStack> itemsMap = new HashMap<>();
    /**
     * Maps slot to a specific piece of code
     */
    private Map<Integer, Consumer<InventoryClickEvent>> actionSlotMap = new HashMap<>();
    /**
     * Maps specific names in the config to code (for example "view-mines" to
     * opening the {@link MinesMenu}
     */
    private Map<String, Consumer<InventoryClickEvent>> actionMap = new HashMap<>();
    private int size;
    private String title;
    private Inventory inventory;
    private Listener listener;
    private ItemStack backgroundItem;

    /**
     * An ItemStack that will be used as a template item (eg in {@link MinesMenu})
     */
    private ItemStack everyItem;

    public MenuSpec() {
        holder = new MenuHolder();
    }

    public void addAction(String name, Consumer<InventoryClickEvent> action) {
        actionMap.put(name, action);
    }

    /**
     * Copy from another MenuSpec
     * This can then have placeholders applied
     *
     * @param other the spec to copy from
     */
    public void copyFrom(MenuSpec other) {
        this.title = other.title;
        this.size = other.size;
        this.itemsMap = new HashMap<>(other.itemsMap);
        this.actionSlotMap = new HashMap<>(other.actionSlotMap);
        this.actionMap = new HashMap<>(other.actionMap);
        this.backgroundItem = other.backgroundItem == null ? null : other.backgroundItem.clone();
        this.everyItem = other.everyItem == null ? null : other.everyItem.clone();
    }

    public void loadFrom(ConfigurationSection section, Object... placeholders) {
        if (section == null) {
            throw new NullPointerException("MenuSpec section is null");
        }

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
        }
        if (section.contains("Background")) {
            backgroundItem = Util.deserializeStack(section.getConfigurationSection("Background").getValues(true));
        }
    }

    /*
        Generates the menu
     */
    public Inventory genMenu() {
        this.inventory = Bukkit.createInventory(holder, size, title);
        itemsMap.entrySet().stream().filter(e -> e.getKey() > 0 && e.getKey() < inventory.getSize())
                .forEach(entry -> inventory.setItem(entry.getKey(), entry.getValue()));

        if (backgroundItem != null)
            IntStream.range(0, size).filter(i -> !itemsMap.containsKey(i))
                    .forEach(i -> inventory.setItem(i, backgroundItem));
        return inventory;
    }

    @SafeVarargs
    public final <T> Inventory genMenu(BiFunction<T, ItemStack, ItemStack> toItem,
                                       Function<T, Consumer<InventoryClickEvent>> clickFunction,
                                       T... data) {

        this.inventory = Bukkit.createInventory(holder, size, title);
        for (int i = 0, tsLength = data.length; i < tsLength; i++) {
            T t = data[i];
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

    /*
      Creates the listeners for the menu
     */
    public Listener toListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!holder.equals(e.getInventory().getHolder())) return;
                e.setCancelled(true);
                Consumer<InventoryClickEvent> consumer = actionSlotMap.get(e.getSlot());
                if (consumer == null) return;
                consumer.accept(e);
            }

            /*
               Clears all the listener handlers because the menu was closed.
             */
            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (!holder.equals(e.getInventory().getHolder())) return;
                InventoryClickEvent.getHandlerList().unregister(listener);
                e.getHandlers().unregister(listener);

                //dereference to allow gc
                listener = null;
                actionMap.clear();
                actionSlotMap.clear();
                itemsMap.clear();
                inventory = null;
                backgroundItem = null;
                everyItem = null;
                title = null;
            }
        };
    }

    /*
       Unknown
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuSpec)) return false;
        MenuSpec menuSpec = (MenuSpec) o;
        return Objects.equals(holder, menuSpec.holder);
    }

    /*
      Unknown
     */
    @Override
    public int hashCode() {
        return Objects.hash(holder);
    }

    /*
      Registers the listeners for the plugin
     */
    public void register(Plugin p) {
        if (listener != null) return;
        listener = toListener();
        Bukkit.getPluginManager().registerEvents(listener, p);
    }

    /*
       Manages the menu holder
     */
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
        @NotNull
        public Inventory getInventory() {
            return inventory;
        }
    }
}

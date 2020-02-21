package me.bristermitten.privatemines;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Util {

    public static Vector toVector(com.sk89q.worldedit.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static com.sk89q.worldedit.Vector toVector(Vector v) {
        return new com.sk89q.worldedit.Vector(v.getX(), v.getY(), v.getZ());
    }

    public static Map<String, Object> serializeStack(ItemStack s) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Amount", s.getAmount());
        map.put("Type", s.getType());
        map.put("Data", s.getDurability());
        if (s.hasItemMeta()) {
            ItemMeta meta = s.getItemMeta();
            map.put("Name", meta.getDisplayName());
            map.put("Lore", meta.getLore());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static ItemStack deserializeStack(Map<String, Object> map, String... placeholders) {
        Map<String, String> mappings = arrayToMap(placeholders);
        ItemStack s = new ItemStack(Material.AIR);
        s.setAmount((Integer) map.getOrDefault("Amount", 1));

        String type = (String) map.getOrDefault("Type", Material.STONE.name());
        type = mappings.getOrDefault(type, type);
        s.setType(Material.matchMaterial(type));

        s.setDurability(((Number) map.getOrDefault("Data", 0)).shortValue());

        ItemMeta itemMeta = s.getItemMeta();
        itemMeta.setDisplayName(color((String) map.get("Name")));
        itemMeta.setLore(color((List<String>) map.get("Lore")));
        s.setItemMeta(itemMeta);
        return s;
    }

    public static String color(String s) {
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> color(List<String> s) {
        if (s == null) return null;
        return s.stream().map(Util::color).collect(Collectors.toList());
    }

    public static Map<String, String> arrayToMap(String... replacements) {
        replacements = Arrays.copyOf(replacements, replacements.length - replacements.length % 2);
        Map<String, String> mapping = new HashMap<>();
        for (int i = 0; i < replacements.length - 1; i++) {
            String key = replacements[i];
            String value = replacements[i + 1];
            mapping.put(key, value);
        }
        return mapping;
    }

    public static com.sk89q.worldedit.Vector deserializeWorldEditVector(Map<String, Object> map) {
        return toVector(Vector.deserialize(map));
    }

    public static void replaceMeta(ItemMeta meta, String... replacements) {
        Map<String, String> replace = arrayToMap(replacements);
        if (meta.hasDisplayName()) {
            replace.forEach((k, v) -> meta.setDisplayName(meta.getDisplayName().replace(k, v)));
        }
        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream()
                    .map(AtomicReference::new)
                    .peek(s -> replace.forEach((k, v) -> s.set(s.get().replace(k, v))))
                    .map(AtomicReference::get)
                    .collect(Collectors.toList()));
        }
    }

    public static String prettify(String s) {
        return WordUtils.capitalize(s.toLowerCase().replace("_", " "));
    }

    public static float getYaw(BlockFace face) {
        switch (face) {
            case WEST:
                return 90f;
            case NORTH:
                return 180f;
            case EAST:
                return -90f;
            case SOUTH:
                return -180f;
            default:
                return 0f;
        }
    }
}


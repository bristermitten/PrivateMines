package me.bristermitten.privatemines;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class Util {

    public static Vector toVector(com.sk89q.worldedit.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static com.sk89q.worldedit.Vector toVector(Vector v) {
        return new com.sk89q.worldedit.Vector(v.getX(), v.getY(), v.getZ());
    }


    @SuppressWarnings("unchecked")
    public static ItemStack deserializeStack(Map<String, Object> map, Object... placeholders) {
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

    public static Map<String, String> arrayToMap(Object... replacements) {

        replacements = Arrays.copyOf(replacements, replacements.length - replacements.length % 2);

        Map<String, String> placeholders = new LinkedHashMap<>(replacements.length / 2);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            placeholders.put(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
        }

        return placeholders;
    }

    public static com.sk89q.worldedit.Vector deserializeWorldEditVector(Map<String, Object> map) {
        return toVector(Vector.deserialize(map));
    }

    public static void replaceMeta(ItemMeta meta, Object... replacements) {
        Map<String, String> replace = arrayToMap(replacements);

        if (meta.hasDisplayName()) {
            replace.forEach((k, v) -> meta.setDisplayName(meta.getDisplayName().replace(k, v)));
        }

        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream()
                    .map(line -> {
                        for (Map.Entry<String, String> entry : replace.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            line = line.replace(key, value);
                        }
                        return line;
                    })
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


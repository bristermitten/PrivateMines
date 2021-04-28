package me.bristermitten.privatemines.util;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import me.bristermitten.privatemines.worldedit.WorldEditVector;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public final class Util {

    private static final Set<Player> onlinePlayers = new HashSet<>();
    private static final Pattern rgbColor = Pattern.compile("(?<!\\\\)(&#[a-fA-F0-9]{6})");

    private Util() {

    }

    public static Vector toBukkitVector(WorldEditVector weVector) {
        return new Vector(weVector.getX(), weVector.getY(), weVector.getZ());
    }

    public static WorldEditVector toWEVector(Vector bukkitVector) {
        return new WorldEditVector(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static WorldEditVector toWEVector(Location bukkitVector) {
        return new WorldEditVector(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static Location toLocation(WorldEditVector weVector, World world) {
        return new Location(world, weVector.getX(), weVector.getY(), weVector.getZ());
    }

    public static BlockVector3 toWGLocation(Location location) {
        return BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static com.sk89q.worldedit.world.World toWEWorld(org.bukkit.World world) {
        return BukkitAdapter.adapt(world);
    }

    public static WorldEditVector deserializeWorldEditVector(Map<String, Object> map) {
        return toWEVector(Vector.deserialize(map));
    }

    public static ItemStack deserializeStack(Map<String, Object> map, Object... placeholders) {
        Map<String, String> replacements = arrayToMap(placeholders);

        ItemStack s = new ItemStack(Material.AIR);
        s.setAmount((int) map.getOrDefault("Amount", 1));
        String type = (String) map.getOrDefault("Type", Material.STONE.name());
        type = replacements.getOrDefault(type, type);
        final Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(type);
        if (!materialOpt.isPresent()) {
            throw new IllegalStateException("Unknown material " + type);
        }
        final XMaterial xMaterial = materialOpt.get();
        s.setType(xMaterial.parseMaterial());
        if (map.containsKey("Data")) {
            s.setDurability((short) (int) map.get("Data"));
        } else {
            s.setDurability(xMaterial.getData());
        }
        ItemMeta itemMeta = s.getItemMeta();

        itemMeta.setDisplayName(color((String) map.get("Name")));


        //noinspection unchecked
        itemMeta.setLore(color((List<String>) map.get("Lore")));

        s.setItemMeta(itemMeta);
        return s;
    }

    public static String color(String s) {
        if (s == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> color(List<String> lines) {
        if (lines == null) {
            return Collections.emptyList();
        }

        return lines.stream().map(Util::color).collect(toList());
    }

    public static String colorRGB(String s) {
        return ChatColor.translateAlternateColorCodes('&', parseRGB(s));
    }

    public static String parseRGB(String msg) {
        Matcher matcher = rgbColor.matcher(msg);
        while(matcher.find()) {
            String color = msg.substring(matcher.start(), matcher.end());
            String hex = color.replace("&", "").toUpperCase();
            msg = msg.replace(color, net.md_5.bungee.api.ChatColor.of(hex).toString());
            matcher = rgbColor.matcher(msg);
        }
        return msg;
    }

    public static Map<String, String> arrayToMap(Object... array) {
        if (array.length == 0) {
            return Collections.emptyMap();
        }
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException("Array size must be a multiple of 2");
        }

        Map<String, String> map = new LinkedHashMap<>(array.length / 2);
        int i = 0;
        while (i < array.length - 1) {
            map.put(array[i].toString(), array[i + 1].toString());
            i += 2;
        }
        return map;
    }

    public static void replaceMeta(ItemMeta meta, Object... replacements) {
        Map<String, String> replace = arrayToMap(replacements);

        if (meta.hasDisplayName()) {
            replace.forEach((k, v) -> meta.setDisplayName(meta.getDisplayName().replace(k, v)));
        }
        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream().map(line -> {
                String[] mutableLine = {line};

                replace.forEach((k, v) -> mutableLine[0] = mutableLine[0].replace(k, v));

                return mutableLine[0];
            }).collect(toList()));
        }
    }

    public static String prettify(String s) {
        return Arrays.stream(s.split("_"))
                .map(String::toLowerCase)
                .map(str -> str.substring(0, 1).toUpperCase() + str.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static List<String> prettifyList(List<String> is) {

        List<String> prettifiedList = new ArrayList();

        Iterator<String> itemStackIterator = is.iterator();
        while (itemStackIterator.hasNext()) {
            System.out.println(itemStackIterator.next());
            Bukkit.broadcastMessage(String.valueOf(itemStackIterator.next()));
            prettifiedList.add(prettify(itemStackIterator.next()));
        }
        return prettifiedList;
    }


    public static Optional<String> getName(ItemStack s) {
        try {
            final XMaterial xMaterial = XMaterial.matchXMaterial(s);
            return Optional.of(prettify(xMaterial.name()));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static Float getYaw(BlockFace face) {
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

    /**
     * I hate this function with a passion
     * Format:
     * "materialName" => new ItemStack(valueOf(name))
     * "materialName/durability" => new ItemStack(valueOf(name), 1, durability)
     *
     * @param s the material name (and optional id) to parse
     * @return an Optional ItemStack based on the data, including Material and possibly durability
     */
    public static Optional<ItemStack> parseItem(String s) {
        String materialName = s;
        short durability = 0;
        if (s.contains("/")) {
            final String[] split = s.split("/");
            materialName = split[0];
            //noinspection UnstableApiUsage
            int data = Optional.ofNullable(Ints.tryParse(split[1])).orElse(0);
            durability = (short) data;
        }
        //Prioritise native material names
        try {
            return Optional.of(new ItemStack(Material.valueOf(materialName), 1, durability));
        } catch (IllegalArgumentException ignored) {
        }
        Optional<XMaterial> m = Optional.ofNullable(Enums.getIfPresent(XMaterial.class, s).orNull());
        if (m.isPresent()) {
            return m.map(XMaterial::parseItem);
        }
        return XMaterial.matchXMaterial(s)
                .map(XMaterial::parseItem);
    }


    public static String parseStyle(String s) {
        switch (s) {
            case "SLOW":
                return "Slow";
            case "FAST":
                return "Fast";
            case "UP_DOWN":
                return "Up down";
            case "DOWN_UP":
                return "Down up";
            case "LEFT_RIGHT":
                return "Left to Right";
            case "RIGHT_LEFT":
                return "Right to Left";
            case "FRONT_BACK":
                return "Front to Back";
            case "BACK_FRONT":
                return "Back to Front";
        }
        return "Unknown reset Style!";
    }


    public static String parsePercent(Integer integer) {
        DecimalFormat format = new DecimalFormat("##.##");
        return format.format(integer);
    }

    private static String readAll(BufferedReader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor completeColor, ChatColor nonCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat("" + completeColor + symbol, progressBars)
                + Strings.repeat("" + nonCompletedColor + symbol, totalBars - progressBars);
    }

    /*
        This is a super basic caching system for the online players saves a tiny bit of milliseconds!
     */

    // Adding a player into the cache system, used mainly in the on player join event

    public static void addToOnlinePlayers(Player player) {
        onlinePlayers.add(player);
    }

    // Remove a player from the cache system, used mainly in the on player leave event

    public static void removeFromOnlinePlayers(Player player) {
        onlinePlayers.remove(player);
    }

    // Gets the online players, this is used in the MineResetTask class so instead of getting
    // all the online players each time possibly causing a tiny spike in the server
    // it will get it from this cache!

    public static Set<Player> getOnlinePlayers() {
        return onlinePlayers;
    }
}
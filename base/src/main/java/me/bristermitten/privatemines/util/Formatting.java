package me.bristermitten.privatemines.util;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Formatting {
    public static List<String> getMineBlocksFormatted(List<ItemStack> stack) {
        return stack.stream()
                .map(Util::getItemName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

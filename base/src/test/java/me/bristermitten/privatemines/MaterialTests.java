package me.bristermitten.privatemines;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MaterialTests {

    @Test
    void test() {
        MockBukkit.mock();
        assertEquals(new ItemStack(Material.NETHERRACK), Util.parseItem("NETHERRACK").orElse(null));
        assertEquals(new ItemStack(Material.NETHERRACK, 1, (short) 3), Util.parseItem("NETHERRACK/3").orElse(null));
        assertEquals(new ItemStack(Material.WOOL, 1, (short) 15), Util.parseItem("BLACK_WOOL").orElse(null));
        assertEquals(new ItemStack(Material.WOOL, 1, (short) 10), Util.parseItem("WOOL/10").orElse(null));
    }
}

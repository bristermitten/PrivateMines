package me.bristermitten.privatemines;

import me.bristermitten.privatemines.worldedit.LegacyWEHook;
import me.bristermitten.privatemines.worldedit.ModernWEHook;

public class Bootstrap {
    public static void main(String[] args) {
        PrivateMines.MINES_FILE_NAME.hashCode();
        new LegacyWEHook();
        new ModernWEHook();
        System.out.println("This class exists to stop shadowJar from minimizing everything :)");
    }
}

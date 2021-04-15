package me.bristermitten.privatemines;

import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.util.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;

public class MineResetTask extends BukkitRunnable {
    private final Plugin plugin;
    private final MineStorage storage;
    private BukkitTask bukkitTask;

    public MineResetTask(Plugin plugin, MineStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /*
    I've made this synchronized because it can stop memory errors.
    */

    public void start() {
        if (bukkitTask == null) {
            bukkitTask = this.runTaskTimer(plugin, 0L, 5 * 20L); // Should fix the lag?
        }
    }

    /*
        I've made this synchronized because it can stop memory errors.
     */

    @Override
    public synchronized void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
    }

    @Override
    public void run() {

        for (PrivateMine mine : storage.getAll()) {
            double total = mine.getTotalBlocks();
            double air = mine.getAirBlocks();
            double percent = air * 100 / total;
            double blocksLeft = 100.00 - percent;

            if (percent <= 0) {
                return;
            }
            DecimalFormat format = new DecimalFormat("##.##");

            Bukkit.broadcastMessage("Mine Percentage (task): " + format.format(percent));
            Bukkit.broadcastMessage("Blocks Left Percent: " + format.format(blocksLeft));

            Bukkit.broadcastMessage(Util.parsePercent((int) percent));
            if (mine.shouldReset() || percent >= 50) {
                mine.fillWE();
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("ACTION_BAR"));
            }
        }
    }
}

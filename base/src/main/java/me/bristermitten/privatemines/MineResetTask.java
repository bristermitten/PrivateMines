package me.bristermitten.privatemines;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
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

    public static com.sk89q.worldedit.world.World toWEWorld(org.bukkit.World world) {
        return BukkitAdapter.adapt(world);
    }

    public void start() {
        if (bukkitTask == null) {
            bukkitTask = this.runTaskTimer(plugin, 0L, 5 * 20L); // Should fix the lag?
        }
    }

    @Override
    public void cancel() {
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
            if (mine.shouldReset() || percent >= 50) {
                mine.fillWE();
            }
        }
    }
}

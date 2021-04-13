package me.bristermitten.privatemines;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
            int total = mine.getTotalBlocks();
            int air = mine.getAirBlocks();
            int percent = air * 100 / total;

            Bukkit.broadcastMessage("Percentage: " + percent);
            if (mine.shouldReset() || percent >= 50) {
                mine.fillWE();
            }
        }
    }
}

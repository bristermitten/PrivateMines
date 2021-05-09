package me.bristermitten.privatemines;

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
            if (mine.shouldReset()) {
                mine.fillWEMultiple(mine.getOwnerPlayer());
            }
        }
    }
}

package me.bristermitten.privatemines;

import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class MineResetTask implements Runnable {
    private final Plugin plugin;
    private final MineStorage storage;
    private BukkitTask bukkitTask;

    public MineResetTask(Plugin plugin, MineStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void start() {
        if (bukkitTask != null) {
            bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 20L);
        }
    }

    public void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }

    }

    @Override
    public void run() {
        for (PrivateMine mine : storage.getAll()) {
            if (mine.shouldReset()) {
                mine.fill(mine.getBlock());
            }
        }
    }
}

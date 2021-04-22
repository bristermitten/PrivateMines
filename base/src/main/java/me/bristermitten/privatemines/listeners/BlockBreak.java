package me.bristermitten.privatemines.listeners;

import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    //TODO FIX THIS UP...

    private final MineStorage storage;
    private final PMConfig config;

    public BlockBreak(MineStorage storage, PMConfig configuration) {
        this.storage = storage;
        this.config = configuration;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

    }
}

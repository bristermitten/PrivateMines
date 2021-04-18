package me.bristermitten.privatemines.listeners;

import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
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
        PrivateMine mine = storage.get(player);


        int total;
        int air;

        if (mine == null) {
            player.sendMessage("Mine is null..");
            return;
        } else {
            /*
                TODO Need to this to do percents.
             */
        }
    }
}

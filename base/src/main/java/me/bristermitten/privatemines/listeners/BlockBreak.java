package me.bristermitten.privatemines.listeners;

import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.text.DecimalFormat;

public class BlockBreak implements Listener {

    private final MineStorage storage;

    public BlockBreak(MineStorage storage) {
        this.storage = storage;
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
            total = mine.getTotalBlocks();
            air = mine.getAirBlocks();
        }
        player.sendMessage("Total blocks: " + total);
        player.sendMessage("Air Blocks: " + air);

        double percent = air * 100.0 / total;
        DecimalFormat format = new DecimalFormat("##.##");

        player.sendMessage("Blocks mined: " + air);
        player.sendMessage("Percentage mined: " + format.format(percent) + "%");
    }
}

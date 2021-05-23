package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("unused") //Most of these methods are called from external programs
public class PrivateMinesUpgradeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    private final PrivateMine privateMine;
    private final Integer resetTime;
    private final MineSchematic mineSchematic;
    private boolean cancelled;
    private List<ItemStack> mineBlocks;

    public PrivateMinesUpgradeEvent(Player player,
                                    PrivateMine privateMine,
                                    List<ItemStack> blocks,
                                    Integer resetTime,
                                    MineSchematic schematic) {
        this.player = player;
        this.privateMine = privateMine;
        this.mineBlocks = blocks;
        this.resetTime = resetTime;
        this.mineSchematic = schematic;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public PrivateMine getPrivateMine() {
        return privateMine;
    }

    public List<ItemStack> getMineBlocks() {
        return this.mineBlocks;
    }

    public void setMineBlocks(List<ItemStack> blocks) {
        this.mineBlocks = blocks;
    }

    public String getUpgradeMineName() {
        return mineSchematic.getName();
    }

    public Integer getResetTime() {
        return this.resetTime;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}

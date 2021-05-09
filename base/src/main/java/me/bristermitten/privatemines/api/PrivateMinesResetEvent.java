package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PrivateMinesResetEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    private final PrivateMine privateMine;

    private boolean cancelled;
    private Double tax;
    private Integer resetTime;

    private List<ItemStack> mineBlocks;

    public Player getPlayer() {
        return player;
    }

    public PrivateMine getPrivateMine() {
        return privateMine;
    }

    public List<ItemStack> getMineBlocks() {
        return this.mineBlocks;
    }

    public Double getTax() {
        return this.tax;
    }

    public Integer getResetTime() {
        return this.resetTime;
    }

    public void setMineBlocks(List<ItemStack> blocks) {
        this.mineBlocks = blocks;
    }

    public void setResetTime(Integer resetTime) {
        this.resetTime = resetTime;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PrivateMinesResetEvent(Player player,
                                     PrivateMine privateMine,
                                     List<ItemStack> blocks,
                                     Integer resetTime) {
        this.player = player;
        this.privateMine = privateMine;
        this.mineBlocks = blocks;
        this.resetTime = resetTime;
    }
}

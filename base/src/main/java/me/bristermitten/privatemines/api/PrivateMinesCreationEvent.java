package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateMinesCreationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    private final PrivateMine privateMine;

    private boolean cancelled;
    private Double tax;
    private Integer resetTime;

    private List<ItemStack> mineBlocks;

    public PrivateMinesCreationEvent(Player player,
                                     PrivateMine privateMine,
                                     List<ItemStack> blocks,
                                     Double tax,
                                     Integer resetTime) {
        this.player = player;
        this.privateMine = privateMine;
        this.mineBlocks = blocks;
        this.tax = tax;
        this.resetTime = resetTime;
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

    public Double getTax() {
        return this.tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Integer getResetTime() {
        return this.resetTime;
    }

    public void setResetTime(Integer resetTime) {
        this.resetTime = resetTime;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}

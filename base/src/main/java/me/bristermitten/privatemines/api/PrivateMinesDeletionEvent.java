package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.List;

public class PrivateMinesDeletionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    private final PrivateMine privateMine;

    private boolean cancelled;
    private List<ItemStack> blocks;
    private Double tax;
    private Integer resetTime;

    public Player getPlayer() {
        return player;
    }

    public PrivateMine getPrivateMine() {
        return privateMine;
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

    public PrivateMinesDeletionEvent(PrivateMine privateMine,
                                     List<ItemStack> blocks,
                                     Double tax,
                                     Player player) {
        this.privateMine = privateMine;
        this.blocks = blocks;
        this.tax = tax;
        this.player = player;
    }
}

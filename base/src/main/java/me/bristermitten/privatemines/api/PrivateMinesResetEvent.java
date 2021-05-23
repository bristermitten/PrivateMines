package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused") //Most of these methods are called from external programs
public class PrivateMinesResetEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    private final PrivateMine privateMine;
    private final List<ItemStack> mineBlocks;
    private boolean cancelled;

    public PrivateMinesResetEvent(Player player,
                                  PrivateMine privateMine,
                                  List<ItemStack> blocks) {
        this.player = player;
        this.privateMine = privateMine;
        this.mineBlocks = blocks;
    }

    @SuppressWarnings("unused") // Bukkit API
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

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}

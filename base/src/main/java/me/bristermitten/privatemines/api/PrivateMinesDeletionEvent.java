package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PrivateMinesDeletionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final PrivateMine privateMine;
    private boolean cancelled;

    public PrivateMinesDeletionEvent(PrivateMine privateMine) {
        this.privateMine = privateMine;
    }

    @SuppressWarnings("unused") //Needed for bukkit event system
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PrivateMine getPrivateMine() {
        return privateMine;
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

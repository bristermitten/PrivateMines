package me.bristermitten.privatemines.data.citizens.ultraprisoncore;

import co.aikar.commands.BukkitCommandManager;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.UUID;

public class UltraPrisonCoreNPCTrait extends Trait implements Listener {
    private final MineStorage storage = PrivateMines.getPlugin().getStorage();

    private final BukkitCommandManager manager = PrivateMines.getPlugin().getManager();

    private final DecimalFormat df = new DecimalFormat("#.##");

    PrivateMine privateMine;

    @Persist("owner")
    private UUID owner;

    private Double tax;

    public UltraPrisonCoreNPCTrait() {
        super("UltraPrisonCoreNPCTrait");
    }

    public UltraPrisonCore getCore() {
        return UltraPrisonCore.getInstance();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return this.owner;
    }
}
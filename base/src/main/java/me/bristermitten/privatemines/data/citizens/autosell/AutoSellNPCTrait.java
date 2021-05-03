package me.bristermitten.privatemines.data.citizens.autosell;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.clip.autosell.events.AutoSellEvent;
import me.clip.autosell.events.SellAllEvent;
import me.clip.autosell.events.SignSellEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class AutoSellNPCTrait extends Trait implements Listener
{

    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    private UUID owner;

    public AutoSellNPCTrait()
    {
        super("AutoSellNPCTrait");
    }

    public void setOwner(UUID owner)
    {
        this.owner = owner;
    }


    static class UUIDPersister implements Persister<UUID> {

        public UUID create(DataKey dataKey) {
            return UUID.fromString(dataKey.getString("UUID"));
        }

        public void save(UUID uuid, DataKey dataKey) {
            dataKey.setString("UUID", uuid.toString());
        }
    }
}
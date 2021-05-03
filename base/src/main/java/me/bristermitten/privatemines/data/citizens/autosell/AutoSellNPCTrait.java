package me.bristermitten.privatemines.data.citizens.autosell;

import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.event.Listener;

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
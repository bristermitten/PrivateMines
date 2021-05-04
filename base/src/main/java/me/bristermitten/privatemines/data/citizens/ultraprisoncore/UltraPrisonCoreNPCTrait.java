package me.bristermitten.privatemines.data.citizens.ultraprisoncore;

import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import java.util.UUID;

public class UltraPrisonCoreNPCTrait extends Trait {

    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    private UUID owner;

    public UltraPrisonCoreNPCTrait() {
        super("UltraPrisonCoreNPCTrait");
    }

    public void setOwner(UUID owner) {
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
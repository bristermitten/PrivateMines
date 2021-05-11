package me.bristermitten.privatemines.data.citizens.ultraprisoncore;

import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.UUIDPersister;
import net.citizensnpcs.api.trait.Trait;

import java.util.UUID;

public class UltraPrisonCoreNPCTrait extends Trait {

    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    @SuppressWarnings("unused")
    private UUID owner;

    public UltraPrisonCoreNPCTrait() {
        super("UltraPrisonCoreNPCTrait");
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}

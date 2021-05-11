package me.bristermitten.privatemines.data.citizens.autosell;

import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.UUIDPersister;
import net.citizensnpcs.api.trait.Trait;

import java.util.UUID;

public class AutoSellNPCTrait extends Trait {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    private UUID owner;

    public AutoSellNPCTrait() {
        super("AutoSellNPCTrait");
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

}

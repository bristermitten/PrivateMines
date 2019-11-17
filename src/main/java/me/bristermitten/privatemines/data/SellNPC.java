//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.data;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class SellNPC {
    private final NPC npc;

    public SellNPC(String name, String skinName, Location location, UUID owner) {
        npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.spawn(location);
        npc.setProtected(true);
        ((SkinnableEntity) npc.getEntity()).setSkinName(skinName);
        npc.getTrait(LookClose.class).toggle();
        SellNPCTrait trait = npc.getTrait(SellNPCTrait.class);
        trait.setOwner(owner);
    }

    public NPC npc() {
        return this.npc;
    }
}

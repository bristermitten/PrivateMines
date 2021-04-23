package me.bristermitten.privatemines.data.ultraprisoncore;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class UltraPrisonCoreNPC {
	private UltraPrisonCoreNPC() {
	}

	/*
	Creates the sell NPC.
	 */

	public static NPC createUPCSellNPC(String name, String skinName, Location location, UUID owner) {
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
		npc.spawn(location);

		npc.setProtected(true);
		((SkinnableEntity) npc.getEntity()).setSkinName(skinName);
		npc.getTrait(LookClose.class).toggle();

		UltraPrisonCoreNPCTrait trait = npc.getTrait(UltraPrisonCoreNPCTrait.class);

		if (trait == null) {
			trait = new UltraPrisonCoreNPCTrait();
			npc.addTrait(trait);
		}

		trait.setOwner(owner);
		return npc;
	}

}

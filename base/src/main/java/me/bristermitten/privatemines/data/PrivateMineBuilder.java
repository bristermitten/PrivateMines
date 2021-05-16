package me.bristermitten.privatemines.data;

import me.bristermitten.privatemines.worldedit.WorldEditRegion;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PrivateMineBuilder {

    public static final class Builder {

        UUID owner;
        List<ItemStack> blocks;
        int mineTier;
        double taxPercent;
        boolean open;
        Location spawnLocation;
        MineLocations mineLocations;
        WorldEditRegion mainRegion;
        IWrappedRegion wgRegion;
        MineSchematic<?> mineSchematic;
        Set<UUID> bannedPlayers;
        Set<UUID> trustedPlayers;

        public Builder(final UUID ownerId,
                       final List<ItemStack> blocks,
                       final int mineTier,
                       final double taxPercent,
                       final boolean open,
                       final Location spawnLocation,
                       final MineLocations mineLocations,
                       final WorldEditRegion worldEditRegion,
                       final IWrappedRegion wgRegion,
                       final MineSchematic<?> mineSchematic,
                       final Set<UUID> bannedPlayers,
                       final Set<UUID> trustedPlayers
        ) {
            this.owner = ownerId;
            this.blocks = blocks;
            this.mineTier = mineTier;
            this.taxPercent = taxPercent;
            this.open = open;
            this.spawnLocation = spawnLocation;
            this.mineLocations = mineLocations;
            this.mainRegion = worldEditRegion;
            this.wgRegion = wgRegion;
            this.mineSchematic = mineSchematic;
            this.bannedPlayers = bannedPlayers;
            this.trustedPlayers = trustedPlayers;
        }

        public Builder setMineTier(final int mineTier) {
            this.mineTier = mineTier;
            return this;
        }

        public Builder setTaxPercentage(final double taxPercent) {
            this.taxPercent = taxPercent;
            return this;
        }

        public Builder setOpen(final boolean open) {
            this.open = open;
            return this;
        }

        public Builder setSpawnLocation(final Location spawnLocation) {
            this.spawnLocation = spawnLocation;
            return this;
        }

        public Builder setLocations(final MineLocations mineLocations) {
            this.mineLocations = mineLocations;
            return this;
        }

        public Builder setWorldEditRegion(final WorldEditRegion worldEditRegion) {
            this.mainRegion = worldEditRegion;
            return this;
        }

        public Builder setWorldGuardRegion(final IWrappedRegion worldGuardRegion) {
            this.wgRegion = worldGuardRegion;
            return this;
        }

        public Builder setMineSchematic(final MineSchematic<?> mineSchematic) {
            this.mineSchematic = mineSchematic;
            return this;
        }

        public Builder setBannedPlayers(final Set<UUID> bannedPlayers) {
            this.bannedPlayers = bannedPlayers;
            return this;
        }

        public Builder setTrustedPlayers(final Set<UUID> trustedPlayers) {
            this.trustedPlayers = trustedPlayers;
            return this;
        }
    }
}

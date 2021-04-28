package me.bristermitten.privatemines.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class EmptyWorldGenerator extends ChunkGenerator {

    @Override
    @NotNull
    public ChunkData generateChunkData(@NotNull World world,
                                       @NotNull Random random,
                                       int cx,
                                       int cz,
                                       @NotNull BiomeGrid biome) {
        return createChunkData(world);
    }
}

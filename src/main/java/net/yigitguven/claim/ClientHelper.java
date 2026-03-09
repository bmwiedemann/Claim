package net.yigitguven.claim;

import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class ClientHelper {
    public static void toggleShowAll() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClaimVisualizer.toggleShowAll();
        }
    }
    public static void addVisual(ChunkPos pos) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClaimVisualizer.addVisual(pos);
        }
    }
}

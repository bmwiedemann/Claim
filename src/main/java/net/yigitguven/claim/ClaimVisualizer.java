package net.yigitguven.claim;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

/**
 * Client-side system to visualize chunk borders using particles.
 */
@Mod.EventBusSubscriber(modid = Claim.MODID, value = Dist.CLIENT)
public class ClaimVisualizer {
    private static final Set<ChunkPos> ACTIVE_VISUALS = new HashSet<>();
    private static boolean showAllMode = false;

    public static void toggleShowAll() {
        showAllMode = !showAllMode;
    }

    public static void addVisual(ChunkPos pos) {
        ACTIVE_VISUALS.add(pos);
    }

    public static void clearVisuals() {
        ACTIVE_VISUALS.clear();
        showAllMode = false;
    }

    private static ChunkPos lastPos = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        ChunkPos currentPos = player.chunkPosition();
        
        // Clear ad-hoc visuals when moving to a new chunk to avoid "stacking"
        if (lastPos != null && !lastPos.equals(currentPos)) {
            ACTIVE_VISUALS.clear();
        }
        lastPos = currentPos;

        if (showAllMode) {
            drawChunkBorders(mc.level, currentPos);
        }

        for (ChunkPos pos : ACTIVE_VISUALS) {
            drawChunkBorders(mc.level, pos);
        }
    }

    private static void drawChunkBorders(Level level, ChunkPos pos) {
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        int maxX = pos.getMaxBlockX() + 1;
        int maxZ = pos.getMaxBlockZ() + 1;
        
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        
        double y = player.getY() + 1.0;

        // Draw horizontal lines at player height
        for (int i = 0; i <= 16; i++) {
            spawnParticle(level, minX + i, y, minZ);
            spawnParticle(level, minX + i, y, maxZ);
            spawnParticle(level, minX, y, minZ + i);
            spawnParticle(level, maxX, y, minZ + i);
        }
    }

    private static void spawnParticle(Level level, double x, double y, double z) {
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }
}

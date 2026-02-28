package net.yigitguven.claim;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.claim.core.ClaimManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Claim.MODID)
public class TeleportManager {
    private static final Map<UUID, PendingTeleport> PENDING_TELEPORTS = new HashMap<>();

    public static void scheduleTeleport(ServerPlayer player, ClaimManager.ClaimTarget target) {
        if (!ModConfig.ENABLE_VISIT.get()) {
            player.sendSystemMessage(Component.literal("Visiting is disabled on this server.").withStyle(ChatFormatting.RED));
            return;
        }

        int ticks = ModConfig.TELEPORT_COOLDOWN.get() * 20;
        if (ticks <= 0) {
            executeTeleport(player, target);
            return;
        }

        PENDING_TELEPORTS.put(player.getUUID(), new PendingTeleport(target, ticks, player.getX(), player.getY(), player.getZ()));
        player.sendSystemMessage(Component.literal("Teleporting in " + ModConfig.TELEPORT_COOLDOWN.get() + " seconds. Don't move!")
                .withStyle(ChatFormatting.AQUA));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<UUID, PendingTeleport>> it = PENDING_TELEPORTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PendingTeleport> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            
            if (player == null) {
                it.remove();
                continue;
            }

            PendingTeleport pending = entry.getValue();
            
            // Movement check
            if (ModConfig.CANCEL_TP_ON_MOVE.get() && (player.getX() != pending.startX || player.getY() != pending.startY || player.getZ() != pending.startZ)) {
                player.sendSystemMessage(Component.literal("Teleport cancelled due to movement.").withStyle(ChatFormatting.RED));
                it.remove();
                continue;
            }

            pending.ticksRemaining--;
            if (pending.ticksRemaining <= 0) {
                executeTeleport(player, pending.target);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingHurtEvent event) {
        if (ModConfig.CANCEL_TP_ON_DAMAGE.get() && event.getEntity() instanceof ServerPlayer player) {
            if (PENDING_TELEPORTS.containsKey(player.getUUID())) {
                PENDING_TELEPORTS.remove(player.getUUID());
                player.sendSystemMessage(Component.literal("Teleport cancelled due to damage.").withStyle(ChatFormatting.RED));
            }
        }
    }

    private static void executeTeleport(ServerPlayer player, ClaimManager.ClaimTarget target) {
        ResourceKey<Level> worldKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(target.dimension()));
        ServerLevel level = player.server.getLevel(worldKey);
        
        if (level != null) {
            double x = target.pos().getMinBlockX() + 8.5;
            double z = target.pos().getMinBlockZ() + 8.5;
            double y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, new net.minecraft.core.BlockPos((int)x, 0, (int)z)).getY() + 1;
            
            player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
            player.sendSystemMessage(Component.literal("Teleported to claim.").withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.literal("Error: Dimension not found.").withStyle(ChatFormatting.RED));
        }
    }

    private static class PendingTeleport {
        final ClaimManager.ClaimTarget target;
        int ticksRemaining;
        final double startX, startY, startZ;

        PendingTeleport(ClaimManager.ClaimTarget target, int ticks, double x, double y, double z) {
            this.target = target;
            this.ticksRemaining = ticks;
            this.startX = x;
            this.startY = y;
            this.startZ = z;
        }
    }
}

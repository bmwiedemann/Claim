package net.yigitguven.claim.events;

import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles protection events for claimed chunks.
 */
@Mod.EventBusSubscriber(modid = Claim.MODID)
public class ClaimEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ModConfig.PROTECT_BLOCKS.get() && isActionRestricted(event.getLevel().getChunk(event.getPos()).getPos(), event.getPlayer(), (Level) event.getLevel(), ClaimData.PermissionLevel.BUILD)) {
            event.setCanceled(true);
            sendDenyMessage(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (ModConfig.PROTECT_BLOCKS.get() && event.getEntity() instanceof Player player) {
            if (isActionRestricted(event.getLevel().getChunk(event.getPos()).getPos(), player, (Level) event.getLevel(), ClaimData.PermissionLevel.BUILD)) {
                event.setCanceled(true);
                sendDenyMessage(player);
            }
        }
    }

    @SubscribeEvent
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        ClaimData.PermissionLevel required = isContainer(event.getLevel(), event.getPos()) ?
                ClaimData.PermissionLevel.CONTAINERS : ClaimData.PermissionLevel.INTERACT;

        if (ModConfig.PROTECT_INTERACT.get() && isActionRestricted(event.getLevel().getChunk(event.getPos()).getPos(), event.getEntity(), event.getLevel(), required)) {
            event.setCanceled(true);
            sendDenyMessage(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (ModConfig.PROTECT_EXPLOSIONS.get()) {
            event.getAffectedBlocks().removeIf(pos -> {
                ClaimData data = ClaimManager.getInstance().getClaim(event.getLevel(), event.getLevel().getChunk(pos).getPos());
                return data != null; // Prevent damage to any claimed chunk
            });
        }
    }

    private static boolean isActionRestricted(ChunkPos pos, Player player, Level level) {
        if (player.hasPermissions(2)) return false; // Admin bypass
        return ClaimManager.getInstance().isProtected(level, pos, player.getUUID());
    }

    private static void sendDenyMessage(Player player) {
        if (player instanceof ServerPlayer) {
            player.sendSystemMessage(Component.literal("You do not have permission to do that here!")
                    .withStyle(ChatFormatting.RED), true);
        }
    }
}

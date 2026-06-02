package net.yigitguven.claim.core;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Claim.MODID)
public class ProtectionHandler {
    private static final Map<UUID, Long> lastDenyMessage = new HashMap<>();
    private static final long DENY_MESSAGE_COOLDOWN_MS = 1000L;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isProtected(event.getPlayer(), event.getPos(), true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isProtected(player, event.getPos(), true)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        
        // 1. Check for standard block interaction (Chests, Doors, etc.)
        if (isProtected(player, pos, false)) {
            event.setCanceled(true);
            return;
        }

        // 2. Check for bucket usage (Filling or Emptying)
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BucketItem || stack.getItem() instanceof SolidBucketItem) {
            BlockPos targetPos = pos.relative(event.getFace());
            if (isProtected(player, targetPos, true)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isProtected(player, event.getPos(), true)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isProtected(Player player, BlockPos pos, boolean isModification) {
        if (player.getCommandSenderWorld().isClientSide) return false;

        boolean isOp = player.hasPermissions(2);
        boolean opBypassConfig = ModConfig.OP_BYPASS.get();

        if (isOp && opBypassConfig) {
            return false;
        }

        ClaimData claim = ClaimManager.getClaimAt(pos);
        if (claim == null) return false;

        UUID playerUUID = player.getUUID();
        if (claim.ownerUUID.equals(playerUUID) || claim.trustedPlayers.contains(playerUUID)) {
            return false;
        }

        // Modifications (Break, Place, Buckets, Trample) are ALWAYS protected
        if (isModification) {
            notifyPlayer(player);
            return true;
        }

        // Interactions (Chests, Doors, etc.) depend on permission mode
        if (claim.permissionMode == ClaimData.PermissionMode.PRIVATE) {
            notifyPlayer(player);
            return true;
        }

        return false;
    }

    private static void notifyPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            long now = System.currentTimeMillis();
            long last = lastDenyMessage.getOrDefault(serverPlayer.getUUID(), 0L);
            if (now - last < DENY_MESSAGE_COOLDOWN_MS)
            {
                return;
            }
            lastDenyMessage.put(serverPlayer.getUUID(), now);
            serverPlayer.displayClientMessage(Component.translatable("message.claim.protection.denied"), true);
        }
    }
}

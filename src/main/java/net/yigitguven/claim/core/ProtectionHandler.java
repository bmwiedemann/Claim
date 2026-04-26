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

import java.util.UUID;

@EventBusSubscriber(modid = Claim.MODID)
public class ProtectionHandler {

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
        
        // Updated Bypass Rules for Testing:
        // 1. Creative mode players NO LONGER bypass protections (for testing purposes).
        // 2. Operators (OP) bypass claims, EXCEPT if their name is "Dev".
        boolean isOp = player.hasPermissions(2);
        boolean isDev = player.getName().getString().equals("Dev");
        
        if (isOp && !isDev) {
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
            serverPlayer.displayClientMessage(Component.literal("§cThis area is claimed!"), true);
        }
    }
}

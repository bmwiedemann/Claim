package net.yigitguven.claim.core;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.config.ModConfig;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = Claim.MODID)
public class ProtectionHandler
{
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (shouldCancel(event.getPlayer(), event.getPos(), true))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            if (shouldCancel(player, event.getPos(), true))
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onFarmlandTrample(FarmlandTrampleEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            if (shouldCancel(player, event.getPos(), true))
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event)
    {
        ItemStack stack = event.getItemStack();
        boolean isBucket = stack.getItem() instanceof BucketItem;
        
        // For interactions (chests, doors) and bucket usage
        if (shouldCancel(event.getEntity(), event.getPos(), isBucket))
        {
            event.setCanceled(true);
        }
    }

    private static boolean shouldCancel(Player player, BlockPos pos, boolean isModification)
    {
        if (player.level().isClientSide) return false;
        
        // OP Bypass
        if (ModConfig.OP_BYPASS.get() && player.hasPermissions(2))
        {
            // Dev testing override
            if (player.getDisplayName().getString().equals("Dev")) return true;
            return false;
        }

        ClaimData claim = ClaimManager.getClaimAt(pos);
        if (claim == null) return false;

        // If player is owner or trusted, always allow
        if (claim.ownerUUID.equals(player.getUUID()) || claim.trustedPlayers.contains(player.getUUID()))
        {
            return false;
        }

        // Home Claim Logic for War Servers
        if (ModConfig.PROTECT_ONLY_HOME_CLAIM.get()) {
            if (!isHomeClaim(claim)) {
                // If it's NOT the home claim, it can be "invaded" (modified/interacted) by anyone
                return false;
            }
        }

        // If it's a modification (break/place/bucket), it's always restricted to owner/trusted
        if (isModification)
        {
            player.displayClientMessage(Component.literal("§cThis area belongs to " + claim.displayName), true);
            return true;
        }

        // For interactions, check PUBLIC/PRIVATE mode
        if (claim.permissionMode == ClaimData.PermissionMode.PRIVATE)
        {
            player.displayClientMessage(Component.literal("§cThis area is private!"), true);
            return true;
        }

        return false;
    }

    private static boolean isHomeClaim(ClaimData claim) {
        List<ClaimData> allClaims = ClaimManager.getClaims();
        return allClaims.stream()
                .filter(c -> c.ownerUUID.equals(claim.ownerUUID))
                .min(Comparator.comparingLong(c -> c.createdAt))
                .map(home -> home.claimId == claim.claimId)
                .orElse(false);
    }
}

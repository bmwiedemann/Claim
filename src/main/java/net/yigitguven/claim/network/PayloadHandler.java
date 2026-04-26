package net.yigitguven.claim.network;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;
import net.yigitguven.claim.core.ClientClaimManager;
import net.yigitguven.claim.core.ClaimManager;
import net.yigitguven.claim.core.PlayerDataManager;
import net.yigitguven.claim.core.ClaimData;

import java.util.List;

public class PayloadHandler
{
    public static void handleSync(final ClaimSyncPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            ClientClaimManager.setClaims(payload.claims());
            ClientClaimManager.setAvailableBlocks(payload.availableBlocks());
            net.yigitguven.claim.integration.XaeroMapIntegration.refresh();
        });
    }

    public static void handleRequestClaim(final RequestClaimPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player)
            {
                int xSize = Math.abs(payload.pos1().getX() - payload.pos2().getX()) + 1;
                int zSize = Math.abs(payload.pos1().getZ() - payload.pos2().getZ()) + 1;
                int area = xSize * zSize;
                
                if (PlayerDataManager.consumeClaimBlocks(player.getUUID(), area)) {
                    boolean success = ClaimManager.addClaim(player.serverLevel(), player.getName().getString() + "'s Claim", player.getUUID(), payload.pos1(), payload.pos2());
                    if (!success) {
                        PlayerDataManager.addClaimBlocks(player.getUUID(), area);
                        player.displayClientMessage(Component.literal("§cClaim failed! Area might be already claimed."), false);
                    } else {
                        player.displayClientMessage(Component.literal("§aClaim created! Used " + area + " blocks."), false);
                    }
                } else {
                    int available = PlayerDataManager.getAvailableBlocks(player.getUUID());
                    player.displayClientMessage(Component.literal("§cNot enough claim blocks! Need: " + area + ", Have: " + available), false);
                }
            }
        });
    }

    public static void handleRequestUnclaim(final RequestUnclaimPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player)
            {
                // Calculate refund before removal
                int refundAmount = 0;
                List<ClaimData> allClaims = ClaimManager.getClaims();
                for (ClaimData claim : allClaims) {
                    if (payload.claimIds().contains(claim.claimId) && claim.ownerUUID.equals(player.getUUID())) {
                        int xSize = Math.abs(claim.pos1.getX() - claim.pos2.getX()) + 1;
                        int zSize = Math.abs(claim.pos1.getZ() - claim.pos2.getZ()) + 1;
                        refundAmount += (xSize * zSize);
                    }
                }
                
                ClaimManager.removeClaims(player.serverLevel(), payload.claimIds(), player.getUUID());
                
                if (refundAmount > 0) {
                    PlayerDataManager.addClaimBlocks(player.getUUID(), refundAmount);
                    player.displayClientMessage(Component.literal("§aUnclaimed! Refunded " + refundAmount + " blocks."), false);
                }
            }
        });
    }

    public static void handleOpenList(final OpenClaimListPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft.getInstance().setScreen(new net.yigitguven.claim.client.ClaimListScreen());
        });
    }

    public static void handleUpdateMetadata(final UpdateClaimMetadataPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player)
            {
                ClaimManager.updateClaimMetadata(player.serverLevel(), 
                    payload.claimId(), payload.name(), payload.description(), 
                    payload.mode(), payload.color(), payload.trustedPlayers(), player.getUUID());
            }
        });
    }
}

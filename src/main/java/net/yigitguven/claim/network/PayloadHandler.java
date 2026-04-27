package net.yigitguven.claim.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yigitguven.claim.core.ClientClaimManager;

public class PayloadHandler
{
    public static void handleSync(final ClaimSyncPayload payload, final IPayloadContext context)
    {
        System.out.println("[Claim] Received sync payload with " + payload.claims().size() + " claims.");
        context.enqueueWork(() -> {
            ClientClaimManager.setClaims(payload.claims());
            System.out.println("[Claim] Claims updated in ClientClaimManager. Refreshing Xaero Map...");
            net.yigitguven.claim.integration.XaeroMapIntegration.refresh();
        });
    }

    public static void handleRequestClaim(final net.yigitguven.claim.network.RequestClaimPayload payload, final IPayloadContext context)
    {
        System.out.println("[Claim] Received request claim payload from " + context.player().getName().getString());
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player)
            {
                System.out.println("[Claim] Adding claim for " + player.getName().getString() + " at " + payload.pos1() + " to " + payload.pos2());
                net.yigitguven.claim.core.ClaimManager.addClaim(player.serverLevel(), player.getName().getString() + "'s Claim", player.getUUID(), payload.pos1(), payload.pos2());
            }
        });
    }

    public static void handleRequestUnclaim(final net.yigitguven.claim.network.RequestUnclaimPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player)
            {
                net.yigitguven.claim.core.ClaimManager.removeClaims(player.serverLevel(), payload.claimIds(), player.getUUID());
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
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player)
            {
                net.yigitguven.claim.core.ClaimManager.updateClaimMetadata(player.serverLevel(), 
                    payload.claimId(), payload.name(), payload.description(), 
                    payload.mode(), payload.color(), payload.trustedPlayers(), player.getUUID());
            }
        });
    }
}

package net.yigitguven.claim.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yigitguven.claim.core.ClientClaimManager;

public class PayloadHandler
{
    public static void handleSync(final ClaimSyncPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            ClientClaimManager.setClaims(payload.claims());
        });
    }

    public static void handleRequestClaim(final net.yigitguven.claim.network.RequestClaimPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player)
            {
                net.yigitguven.claim.core.ClaimManager.addClaim(player.serverLevel(), player.getName().getString() + "'s Claim", player.getUUID(), payload.pos1(), payload.pos2());
            }
        });
    }
}

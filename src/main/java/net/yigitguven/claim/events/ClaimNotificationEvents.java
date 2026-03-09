package net.yigitguven.claim.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.ModConfig;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles action bar notifications when players enter or leave claimed chunks.
 */
@EventBusSubscriber(modid = Claim.MODID)
public class ClaimNotificationEvents {
    private static final Map<UUID, ChunkPos> PLAYER_CHUNKS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!ModConfig.ENABLE_ACTION_BAR_NOTIFICATIONS.get()) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ChunkPos currentPos = player.chunkPosition();
        ChunkPos lastPos = PLAYER_CHUNKS.get(player.getUUID());

        if (lastPos == null || !lastPos.equals(currentPos)) {
            PLAYER_CHUNKS.put(player.getUUID(), currentPos);
            handleChunkChange(player, lastPos, currentPos);
        }
    }

    private static void handleChunkChange(ServerPlayer player, ChunkPos from, ChunkPos to) {
        ClaimData fromData = from != null ? ClaimManager.getInstance().getClaim(player.level(), from) : null;
        ClaimData toData = ClaimManager.getInstance().getClaim(player.level(), to);

        // Check if we changed owners/claims
        boolean wasInClaim = fromData != null;
        boolean isInClaim = toData != null;

        if (isInClaim) {
            // Check if entering a NEW claim or just a chunk of the SAME claim
            if (!wasInClaim || !fromData.equals(toData)) {
                String name = toData.getName() != null ? toData.getName() : "Unnamed";
                String owner = toData.getOwnerName();
                player.displayClientMessage(Component.literal("Entering: ")
                        .append(Component.literal(name).withStyle(ChatFormatting.GOLD))
                        .append(" (Owner: " + owner + ")"), true);
            }
        } else if (wasInClaim) {
            String name = fromData.getName() != null ? fromData.getName() : "Unnamed";
            player.displayClientMessage(Component.literal("Leaving: ")
                    .append(Component.literal(name).withStyle(ChatFormatting.YELLOW)), true);
        }
    }
}

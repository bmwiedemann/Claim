package net.yigitguven.claim.events;

import net.yigitguven.claim.config.ModConfig;
import net.yigitguven.claim.core.SelectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Claim.MODID)
public class PlayerEvents
{
    private static final Map<UUID, Integer> lastPlayerClaim = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event)
    {
        if (event.getEntity().level().isClientSide) return;

        Player player = event.getEntity();
        BlockPos pos = player.blockPosition();
        ClaimData currentClaim = ClaimManager.getClaimAt(pos);
        int currentClaimId = currentClaim != null ? currentClaim.claimId : -1;

        Integer lastId = lastPlayerClaim.get(player.getUUID());
        if (lastId == null) lastId = -1;

        if (currentClaimId != lastId)
        {
            handleClaimChange(player, lastId, currentClaim);
            lastPlayerClaim.put(player.getUUID(), currentClaimId);
        }
    }

    private static void handleClaimChange(Player player, int lastId, ClaimData currentClaim)
    {
        if (currentClaim != null)
        {
            // Entered a claim
            String message = "§aEntering: §f" + currentClaim.displayName;
            sendIndicator(player, message, true, currentClaim.displayName);
        }
        else
        {
            // Left a claim
            String message = "§cLeaving claim area";
            sendIndicator(player, message, false, "");
        }
    }

    private static void sendIndicator(Player player, String message, boolean entering, String claimName)
    {
        if (ModConfig.SHOW_CLAIM_ACTION_BAR.get())
        {
            player.displayClientMessage(Component.literal(message), true);
        }

        if (ModConfig.SHOW_CLAIM_CHAT.get())
        {
            player.displayClientMessage(Component.literal(message), false);
        }

        if (entering && ModConfig.SHOW_CLAIM_TITLE.get() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
        {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("§a" + claimName)));
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("§7Claimed Area")));
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        String toolId = ModConfig.SELECTION_TOOL.get();
        String heldItemId = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString();

        if (heldItemId.equals(toolId))
        {
            BlockPos pos = event.getPos();
            SelectionManager.Selection selection = SelectionManager.getOrCreateSelection(player.getUUID());

            if (player.isShiftKeyDown())
            {
                selection.reset();
                player.displayClientMessage(Component.literal("Selection reset."), true);
            }
            else
            {
                if (selection.pos1 == null)
                {
                    selection.pos1 = pos;
                    player.displayClientMessage(Component.literal("Position 1 set: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
                }
                else if (selection.pos2 == null)
                {
                    selection.pos2 = pos;
                    player.displayClientMessage(Component.literal("Position 2 set: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
                }
                else
                {
                    selection.pos1 = pos;
                    selection.pos2 = null;
                    player.displayClientMessage(Component.literal("Position 1 set (Selection restarted): " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
                }
            }
            event.setCanceled(true); 
        }
    }
}

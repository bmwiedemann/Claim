package net.yigitguven.claim.events;

import net.yigitguven.claim.config.ModConfig;
import net.yigitguven.claim.core.SelectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.yigitguven.claim.Claim;

@EventBusSubscriber(modid = Claim.MODID)
public class PlayerEvents
{
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

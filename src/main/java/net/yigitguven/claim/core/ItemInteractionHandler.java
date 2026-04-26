package net.yigitguven.claim.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.config.ModConfig;

@EventBusSubscriber(modid = Claim.MODID)
public class ItemInteractionHandler {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;
        
        String itemId = ModConfig.CLAIM_BLOCK_ITEM.get();
        if (itemId == null || itemId.isEmpty() || itemId.equalsIgnoreCase("none")) return;
        
        ItemStack stack = event.getItemStack();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        
        if (id.toString().equals(itemId)) {
            Player player = event.getEntity();
            int amount = ModConfig.BLOCKS_PER_ITEM.get();
            
            if (PlayerDataManager.addClaimBlocks(player.getUUID(), amount)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aConsumed " + stack.getItem().getName(stack).getString() + "! Gained " + amount + " claim blocks."), true);
                
                if (player instanceof ServerPlayer serverPlayer) {
                    Claim.syncClaims(serverPlayer);
                }
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cYou have reached the maximum claim block limit!"), true);
            }
            
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}

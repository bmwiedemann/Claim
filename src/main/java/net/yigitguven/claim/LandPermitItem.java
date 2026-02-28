package net.yigitguven.claim;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.yigitguven.claim.core.ClaimManager;
import net.yigitguven.claim.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LandPermitItem extends Item {
    public LandPermitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Check config
                if (!ModConfig.USE_ITEMS_FOR_CLAIM.get()) {
                    player.sendSystemMessage(Component.literal("Item-based claiming is disabled on this server.")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }

                ChunkPos pos = level.getChunkAt(context.getClickedPos()).getPos();
                // Claim logic
                if (ClaimManager.getInstance().claim(level, pos, player.getUUID(), player.getScoreboardName())) {
                    player.sendSystemMessage(Component.literal("Successfully claimed this chunk!")
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    player.sendSystemMessage(Component.literal("Failed to claim (already claimed or limit reached).")
                            .withStyle(ChatFormatting.RED));
                }
            } else {
                player.sendSystemMessage(Component.literal("Use /claim rename <name> while holding this permit to rename your claim.")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

package net.yigitguven.claim;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;
import net.yigitguven.claim.ClaimVisualizer;
import net.yigitguven.claim.ModConfig;

public class SurveyorCompassItem extends Item {
    public SurveyorCompassItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClaimVisualizer::toggleShowAll);
            player.displayClientMessage(Component.literal("Toggled chunk border visualization.")
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ChunkPos pos = level.getChunkAt(context.getClickedPos()).getPos();
        
        if (!level.isClientSide) {
            // Info logic
            ClaimData data = ClaimManager.getInstance().getClaim(level, pos);
            if (data != null) {
                String ownerDisplay = data.getOwnerName();
                String nameDisplay = data.getName() != null ? " (" + data.getName() + ")" : "";
                player.sendSystemMessage(Component.literal("Claimed by: ")
                        .append(Component.literal(ownerDisplay + nameDisplay).withStyle(ChatFormatting.GOLD)));
            } else {
                player.sendSystemMessage(Component.literal("This chunk is not claimed.")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClaimVisualizer.addVisual(pos));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

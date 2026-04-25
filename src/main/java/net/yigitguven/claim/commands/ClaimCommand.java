package net.yigitguven.claim.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yigitguven.claim.core.ClaimManager;
import net.yigitguven.claim.core.SelectionManager;

public class ClaimCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("claim")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    SelectionManager.Selection selection = SelectionManager.getOrCreateSelection(player.getUUID());

                    if (selection.isComplete())
                    {
                        ClaimManager.addClaim(context.getSource().getLevel(), player.getScoreboardName() + "'s Claim", player.getUUID(), selection.pos1, selection.pos2);
                        player.displayClientMessage(Component.literal("Area claimed successfully!"), false);
                        selection.reset();
                        return 1;
                    }
                    else
                    {
                        player.displayClientMessage(Component.literal("Please select two positions first using a wooden shovel."), false);
                        return 0;
                    }
                })
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    BlockPos pos1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
                    BlockPos pos2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");

                    ClaimManager.addClaim(context.getSource().getLevel(), player.getScoreboardName() + "'s Claim", player.getUUID(), pos1, pos2);
                    player.displayClientMessage(Component.literal("Area claimed successfully via command!"), false);
                    return 1;
                })))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.connection.send(new net.yigitguven.claim.network.OpenClaimListPayload());
                            return 1;
                        })));
    }
}

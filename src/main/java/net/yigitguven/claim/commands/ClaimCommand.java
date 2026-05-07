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
import net.yigitguven.claim.config.ModConfig;

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
                        int maxClaims = ModConfig.MAX_CLAIMS.get();
                        long maxBlocksPerClaim = ModConfig.MAX_BLOCKS_PER_CLAIM.get();
                        long maxTotalBlocks = ModConfig.MAX_TOTAL_BLOCKS.get();
                        
                        boolean isOp = player.hasPermissions(2);
                        boolean bypass = isOp && ModConfig.OP_BYPASS.get();

                        if (!bypass)
                        {
                            if (maxClaims != -1 && ClaimManager.getClaimCount(player.getUUID()) >= maxClaims)
                            {
                                player.displayClientMessage(Component.literal("You have reached the maximum number of claims (" + maxClaims + ")!"), false);
                                return 0;
                            }

                            long newClaimBlocks = ClaimManager.calculateVolume(context.getSource().getLevel(), selection.pos1, selection.pos2);
                            if (maxBlocksPerClaim != -1 && newClaimBlocks > maxBlocksPerClaim)
                            {
                                player.displayClientMessage(Component.literal("This area is too large! Max blocks per claim: " + maxBlocksPerClaim + " (Current: " + newClaimBlocks + ")"), false);
                                return 0;
                            }

                            long currentTotal = ClaimManager.getTotalClaimedBlocks(player.getUUID());
                            if (maxTotalBlocks != -1 && (currentTotal + newClaimBlocks) > maxTotalBlocks)
                            {
                                player.displayClientMessage(Component.literal("You don't have enough claim blocks left! Total limit: " + maxTotalBlocks + " (Current: " + currentTotal + ", Needed: " + newClaimBlocks + ")"), false);
                                return 0;
                            }
                        }

                        if (ClaimManager.addClaim(context.getSource().getLevel(), player.getScoreboardName() + "'s Claim", player.getUUID(), selection.pos1, selection.pos2))
                        {
                            player.displayClientMessage(Component.literal("Area claimed successfully!"), false);
                            selection.reset();
                            return 1;
                        }
                        else
                        {
                            player.displayClientMessage(Component.literal("This area (or part of it) is already claimed!"), false);
                            return 0;
                        }
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

                    int maxClaims = ModConfig.MAX_CLAIMS.get();
                    long maxBlocksPerClaim = ModConfig.MAX_BLOCKS_PER_CLAIM.get();
                    long maxTotalBlocks = ModConfig.MAX_TOTAL_BLOCKS.get();

                    boolean isOp = player.hasPermissions(2);
                    boolean bypass = isOp && ModConfig.OP_BYPASS.get();

                    if (!bypass)
                    {
                        if (maxClaims != -1 && ClaimManager.getClaimCount(player.getUUID()) >= maxClaims)
                        {
                            player.displayClientMessage(Component.literal("You have reached the maximum number of claims (" + maxClaims + ")!"), false);
                            return 0;
                        }

                        long newClaimBlocks = ClaimManager.calculateVolume(context.getSource().getLevel(), pos1, pos2);
                        if (maxBlocksPerClaim != -1 && newClaimBlocks > maxBlocksPerClaim)
                        {
                            player.displayClientMessage(Component.literal("This area is too large! Max blocks per claim: " + maxBlocksPerClaim + " (Current: " + newClaimBlocks + ")"), false);
                            return 0;
                        }

                        long currentTotal = ClaimManager.getTotalClaimedBlocks(player.getUUID());
                        if (maxTotalBlocks != -1 && (currentTotal + newClaimBlocks) > maxTotalBlocks)
                        {
                            player.displayClientMessage(Component.literal("You don't have enough claim blocks left! Total limit: " + maxTotalBlocks + " (Current: " + currentTotal + ", Needed: " + newClaimBlocks + ")"), false);
                            return 0;
                        }
                    }

                    if (ClaimManager.addClaim(context.getSource().getLevel(), player.getScoreboardName() + "'s Claim", player.getUUID(), pos1, pos2))
                    {
                        player.displayClientMessage(Component.literal("Area claimed successfully via command!"), false);
                        return 1;
                    }
                    else
                    {
                        player.displayClientMessage(Component.literal("This area (or part of it) is already claimed!"), false);
                        return 0;
                    }
                })))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.connection.send(new net.yigitguven.claim.network.OpenClaimListPayload());
                            return 1;
                        })));
    }
}

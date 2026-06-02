package net.yigitguven.claim.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yigitguven.claim.config.ModConfig;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;
import net.yigitguven.claim.core.SelectionManager;
import net.yigitguven.claim.network.OpenClaimListPayload;
import net.yigitguven.claim.registry.ModItems;

public class ClaimCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("claim")
                .executes(context -> createClaimFromSelection(context.getSource().getPlayerOrException(), context.getSource()))
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    BlockPos pos1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
                                    BlockPos pos2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");
                                    return createClaimFromPositions(player, context.getSource(), pos1, pos2);
                                })))
                .then(Commands.literal("help")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.displayClientMessage(Component.translatable("message.claim.help.header"), false);
                            player.displayClientMessage(Component.translatable("message.claim.help.main"), false);
                            player.displayClientMessage(Component.translatable("message.claim.help.manage"), false);
                            player.displayClientMessage(Component.translatable("message.claim.help.visit"), false);
                            player.displayClientMessage(Component.translatable("message.claim.help.trust"), false);
                            player.displayClientMessage(Component.translatable("message.claim.help.admin"), false);
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.connection.send(new OpenClaimListPayload());
                            return 1;
                        }))
                .then(Commands.literal("friend")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();

                                    if (ModConfig.LOCK_CLAIM_TRUSTED.get() && !bypass)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.locked"), false);
                                        return 0;
                                    }

                                    if (target.getUUID().equals(player.getUUID()))
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.self"), false);
                                        return 0;
                                    }

                                    int updated = ClaimManager.addTrustedToAllClaims(context.getSource().getLevel(), player.getUUID(), target.getUUID());
                                    if (updated == 0)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.none"), false);
                                        return 0;
                                    }

                                    player.displayClientMessage(Component.translatable("message.claim.trustall.success", target.getScoreboardName(), updated), false);
                                    return updated;
                                })))
                .then(Commands.literal("trustall")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();

                                    if (ModConfig.LOCK_CLAIM_TRUSTED.get() && !bypass)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.locked"), false);
                                        return 0;
                                    }

                                    if (target.getUUID().equals(player.getUUID()))
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.self"), false);
                                        return 0;
                                    }

                                    int updated = ClaimManager.addTrustedToAllClaims(context.getSource().getLevel(), player.getUUID(), target.getUUID());
                                    if (updated == 0)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.none"), false);
                                        return 0;
                                    }

                                    player.displayClientMessage(Component.translatable("message.claim.trustall.success", target.getScoreboardName(), updated), false);
                                    return updated;
                                })))
                .then(Commands.literal("untrustall")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();

                                    if (ModConfig.LOCK_CLAIM_TRUSTED.get() && !bypass)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.locked"), false);
                                        return 0;
                                    }

                                    int updated = ClaimManager.removeTrustedFromAllClaims(context.getSource().getLevel(), player.getUUID(), target.getUUID());
                                    if (updated == 0)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.untrustall.none", target.getScoreboardName()), false);
                                        return 0;
                                    }

                                    player.displayClientMessage(Component.translatable("message.claim.untrustall.success", target.getScoreboardName(), updated), false);
                                    return updated;
                                })))
                .then(Commands.literal("unfriend")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();

                                    if (ModConfig.LOCK_CLAIM_TRUSTED.get() && !bypass)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.trustall.locked"), false);
                                        return 0;
                                    }

                                    int updated = ClaimManager.removeTrustedFromAllClaims(context.getSource().getLevel(), player.getUUID(), target.getUUID());
                                    if (updated == 0)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.untrustall.none", target.getScoreboardName()), false);
                                        return 0;
                                    }

                                    player.displayClientMessage(Component.translatable("message.claim.untrustall.success", target.getScoreboardName(), updated), false);
                                    return updated;
                                })))
                .then(Commands.literal("info")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ClaimData claim = ClaimManager.getClaimAt(player.blockPosition());
                            if (claim == null)
                            {
                                player.displayClientMessage(Component.translatable("message.claim.info.none"), false);
                                return 0;
                            }

                            String ownerName = player.getServer() != null
                                    ? player.getServer().getProfileCache().get(claim.ownerUUID).map(profile -> profile.getName()).orElse(claim.ownerUUID.toString())
                                    : claim.ownerUUID.toString();
                            player.displayClientMessage(Component.translatable("message.claim.info.details", claim.claimId, claim.displayName, ownerName), false);
                            return 1;
                        }))
                .then(Commands.literal("unclaim")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ClaimData claim = ClaimManager.getClaimAt(player.blockPosition());

                            if (claim == null)
                            {
                                player.displayClientMessage(Component.translatable("message.claim.unclaim.none_here"), false);
                                return 0;
                            }

                            boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();
                            if (!claim.ownerUUID.equals(player.getUUID()) && !bypass)
                            {
                                player.displayClientMessage(Component.translatable("message.claim.unclaim.owner_only"), false);
                                return 0;
                            }

                            boolean removed = ClaimManager.removeClaim(context.getSource().getLevel(), claim.claimId);
                            if (removed)
                            {
                                player.displayClientMessage(Component.translatable("message.claim.unclaim.success"), false);
                                return 1;
                            }

                            player.displayClientMessage(Component.translatable("message.claim.unclaim.failed"), false);
                            return 0;
                        }))
                .then(Commands.literal("setvisit")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ClaimData claim = ClaimManager.getClaimAt(player.blockPosition());

                            if (claim == null)
                            {
                                player.displayClientMessage(Component.translatable("message.claim.setvisit.must_be_inside"), false);
                                return 0;
                            }

                            boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();
                            if (!claim.ownerUUID.equals(player.getUUID()) && !bypass)
                            {
                                player.displayClientMessage(Component.translatable("message.claim.setvisit.owner_only"), false);
                                return 0;
                            }

                            BlockPos visitPos = player.blockPosition();
                            if (ClaimManager.setVisitPos(context.getSource().getLevel(), claim.claimId, player.getUUID(), visitPos, bypass))
                            {
                                player.displayClientMessage(Component.translatable("message.claim.setvisit.updated", claim.claimId), false);
                                return 1;
                            }

                            player.displayClientMessage(Component.translatable("message.claim.setvisit.failed"), false);
                            return 0;
                        }))
                .then(Commands.literal("visit")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.connection.send(new OpenClaimListPayload());
                            player.displayClientMessage(Component.translatable("message.claim.visit.open_list"), false);
                            return 1;
                        })
                        .then(Commands.argument("claimId", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int claimId = IntegerArgumentType.getInteger(context, "claimId");
                                    ClaimData claim = ClaimManager.getClaimById(claimId);

                                    if (claim == null)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.visit.not_found", claimId), false);
                                        return 0;
                                    }

                                    boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();
                                    if (ModConfig.VISIT_OWNER_ONLY.get() && !claim.ownerUUID.equals(player.getUUID()) && !bypass)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.visit.owner_only"), false);
                                        return 0;
                                    }

                                    boolean canVisit = bypass
                                            || claim.ownerUUID.equals(player.getUUID())
                                            || claim.trustedPlayers.contains(player.getUUID())
                                            || claim.permissionMode == ClaimData.PermissionMode.PUBLIC;
                                    if (!canVisit)
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.visit.no_permission"), false);
                                        return 0;
                                    }

                                    BlockPos target = ClaimManager.getVisitPos(context.getSource().getLevel(), claim);
                                    player.teleportTo(context.getSource().getLevel(), target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, player.getYRot(), player.getXRot());
                                    player.displayClientMessage(Component.translatable("message.claim.visit.success", claim.claimId, claim.displayName), false);
                                    return 1;
                                })))
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("givepermit")
                                .executes(context -> giveItemToPlayer(context.getSource(), context.getSource().getPlayerOrException(), ModItems.LAND_PERMIT_ID, 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> giveItemToPlayer(context.getSource(), context.getSource().getPlayerOrException(), ModItems.LAND_PERMIT_ID, IntegerArgumentType.getInteger(context, "amount"))))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> giveItemToPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), ModItems.LAND_PERMIT_ID, 1))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveItemToPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), ModItems.LAND_PERMIT_ID, IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("givecompass")
                                .executes(context -> giveItemToPlayer(context.getSource(), context.getSource().getPlayerOrException(), ModItems.CLAIM_COMPASS_ID, 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> giveItemToPlayer(context.getSource(), context.getSource().getPlayerOrException(), ModItems.CLAIM_COMPASS_ID, IntegerArgumentType.getInteger(context, "amount"))))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> giveItemToPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), ModItems.CLAIM_COMPASS_ID, 1))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveItemToPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), ModItems.CLAIM_COMPASS_ID, IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("unclaim")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ClaimData claim = ClaimManager.getClaimAt(player.blockPosition());
                                    if (claim != null)
                                    {
                                        ClaimManager.removeClaim(context.getSource().getLevel(), claim.claimId);
                                        player.displayClientMessage(Component.translatable("message.claim.admin.unclaim.success"), false);
                                        return 1;
                                    }
                                    else
                                    {
                                        player.displayClientMessage(Component.translatable("message.claim.admin.unclaim.none_here"), false);
                                        return 0;
                                    }
                                }))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            ClaimManager.clearPlayerClaims(context.getSource().getLevel(), target.getUUID());
                                            context.getSource().sendSuccess(() -> Component.translatable("message.claim.admin.clear.success", target.getScoreboardName()), true);
                                            return 1;
                                        })))));
    }

    private static int createClaimFromSelection(ServerPlayer player, CommandSourceStack source)
    {
        SelectionManager.Selection selection = SelectionManager.getOrCreateSelection(player.getUUID());
        if (!selection.isComplete())
        {
            player.displayClientMessage(Component.translatable("message.claim.selection_required"), false);
            player.displayClientMessage(Component.translatable("message.claim.selection.hint_help"), false);
            return 0;
        }

        int result = createClaimFromPositions(player, source, selection.pos1, selection.pos2);
        if (result > 0)
        {
            selection.reset();
        }
        return result;
    }

    private static int createClaimFromPositions(ServerPlayer player, CommandSourceStack source, BlockPos pos1, BlockPos pos2)
    {
        int maxClaims = ModConfig.MAX_CLAIMS.get();
        long maxBlocksPerClaim = ModConfig.MAX_BLOCKS_PER_CLAIM.get();
        long maxTotalBlocks = ModConfig.MAX_TOTAL_BLOCKS.get();

        boolean bypass = player.hasPermissions(2) && ModConfig.OP_BYPASS.get();

        if (!bypass)
        {
            if (maxClaims != -1 && ClaimManager.getClaimCount(player.getUUID()) >= maxClaims)
            {
                player.displayClientMessage(Component.translatable("message.claim.max_claims", maxClaims), false);
                return 0;
            }

            long newClaimBlocks = ClaimManager.calculateVolume(source.getLevel(), pos1, pos2);
            if (maxBlocksPerClaim != -1 && newClaimBlocks > maxBlocksPerClaim)
            {
                player.displayClientMessage(Component.translatable("message.claim.max_blocks_per_claim", maxBlocksPerClaim, newClaimBlocks), false);
                return 0;
            }

            long currentTotal = ClaimManager.getTotalClaimedBlocks(player.getUUID());
            if (maxTotalBlocks != -1 && (currentTotal + newClaimBlocks) > maxTotalBlocks)
            {
                player.displayClientMessage(Component.translatable("message.claim.max_total_blocks", maxTotalBlocks, currentTotal, newClaimBlocks), false);
                return 0;
            }

            if (!ClaimManager.hasRequiredLandPermit(player))
            {
                player.displayClientMessage(Component.translatable("message.claim.permit_required", ModConfig.LAND_PERMIT_AMOUNT.get(), ModConfig.LAND_PERMIT_ITEM.get()), false);
                return 0;
            }
        }

        if (ClaimManager.addClaim(source.getLevel(), player.getScoreboardName() + "'s Claim", player.getUUID(), pos1, pos2))
        {
            if (!bypass)
            {
                ClaimManager.consumeLandPermit(player);
            }
            player.displayClientMessage(Component.translatable("message.claim.created"), false);
            return 1;
        }

        player.displayClientMessage(Component.translatable("message.claim.already_claimed"), false);
        return 0;
    }

    private static int giveItemToPlayer(CommandSourceStack source, ServerPlayer target, ResourceLocation itemId, int amount)
    {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR)
        {
            source.sendFailure(Component.translatable("message.claim.admin.give.missing_item", itemId.toString()));
            return 0;
        }

        ItemStack stack = new ItemStack(item, amount);
        boolean added = target.getInventory().add(stack);
        if (!added)
        {
            target.drop(stack, false);
        }

        target.displayClientMessage(Component.translatable("message.claim.admin.give.success", amount, item.getDescription()), false);
        if (source.getEntity() != target)
        {
            source.sendSuccess(() -> Component.translatable("message.claim.admin.give.sent", amount, item.getDescription(), target.getScoreboardName()), true);
        }
        return amount;
    }
}

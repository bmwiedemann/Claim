package net.yigitguven.claim.commands;

import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/**
 * Handles mod commands.
 */
public class ClaimCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .executes(context -> claimChunk(context.getSource()))
            .then(Commands.literal("unclaim")
                .executes(context -> unclaimChunk(context.getSource(), false)))
            .then(Commands.literal("info")
                .executes(context -> getInfo(context.getSource())))
            .then(Commands.literal("list")
                .executes(context -> listClaims(context.getSource())))
            .then(Commands.literal("trust")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> trustPlayer(context.getSource(), EntityArgument.getPlayer(context, "player")))))
            .then(Commands.literal("untrust")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> untrustPlayer(context.getSource(), EntityArgument.getPlayer(context, "player")))))
            .then(Commands.literal("admin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("unclaim")
                    .executes(context -> unclaimChunk(context.getSource(), true))))
        );
    }

    private static int claimChunk(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ChunkPos pos = player.chunkPosition();
            if (ClaimManager.getInstance().claim(player.level(), pos, player.getUUID(), player.getScoreboardName())) {
                source.sendSuccess(() -> Component.literal("Successfully claimed this chunk!")
                        .withStyle(ChatFormatting.GREEN), false);
            } else {
                source.sendFailure(Component.literal("This chunk is already claimed!")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int unclaimChunk(CommandSourceStack source, boolean isAdmin) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ChunkPos pos = player.chunkPosition();
            if (ClaimManager.getInstance().unclaim(player.level(), pos, player.getUUID(), isAdmin)) {
                source.sendSuccess(() -> Component.literal("Successfully unclaimed this chunk!")
                        .withStyle(ChatFormatting.GREEN), false);
            } else {
                source.sendFailure(Component.literal("You don't own this claim!")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int getInfo(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ChunkPos pos = player.chunkPosition();
            ClaimData data = ClaimManager.getInstance().getClaim(player.level(), pos);
            if (data != null) {
                source.sendSuccess(() -> Component.literal("This chunk is claimed by: ")
                        .append(Component.literal(data.getOwnerName()).withStyle(ChatFormatting.GOLD)), false);
            } else {
                source.sendSuccess(() -> Component.literal("This chunk is not claimed."), false);
            }
        }
        return 1;
    }

    private static int listClaims(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            int count = ClaimManager.getInstance().getPlayerClaimCount(player.level(), player.getUUID());
            source.sendSuccess(() -> Component.literal("You have ")
                    .append(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.GOLD))
                    .append(" active claims."), false);
        }
        return 1;
    }

    private static int trustPlayer(CommandSourceStack source, ServerPlayer target) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ChunkPos pos = player.chunkPosition();
            ClaimData data = ClaimManager.getInstance().getClaim(player.level(), pos);
            if (data != null && data.getOwnerUUID().equals(player.getUUID())) {
                data.trust(target.getUUID());
                ClaimManager.getInstance().save();
                source.sendSuccess(() -> Component.literal("Successfully trusted ")
                        .append(Component.literal(target.getScoreboardName()).withStyle(ChatFormatting.GOLD))
                        .append(" in this chunk."), false);
            } else {
                source.sendFailure(Component.literal("You must stand in your own claim to trust someone!")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int untrustPlayer(CommandSourceStack source, ServerPlayer target) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ChunkPos pos = player.chunkPosition();
            ClaimData data = ClaimManager.getInstance().getClaim(player.level(), pos);
            if (data != null && data.getOwnerUUID().equals(player.getUUID())) {
                data.untrust(target.getUUID());
                ClaimManager.getInstance().save();
                source.sendSuccess(() -> Component.literal("Successfully untrusted ")
                        .append(Component.literal(target.getScoreboardName()).withStyle(ChatFormatting.GOLD))
                        .append(" from this chunk."), false);
            } else {
                source.sendFailure(Component.literal("You must stand in your own claim to untrust someone!")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }
}

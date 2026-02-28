package net.yigitguven.claim.commands;

import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClaimManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.yigitguven.claim.TeleportManager;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.ModConfig;

/**
 * Handles mod commands.
 */
public class ClaimCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .executes(context -> claimChunk(context.getSource(), null))
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(context -> claimChunk(context.getSource(), StringArgumentType.getString(context, "name"))))
            .then(Commands.literal("rename")
                .then(Commands.argument("name", StringArgumentType.string())
                    .executes(context -> renameClaim(context.getSource(), StringArgumentType.getString(context, "name")))))
            .then(Commands.literal("visit")
                .then(Commands.argument("claim", StringArgumentType.string())
                    .executes(context -> visitClaim(context.getSource(), StringArgumentType.getString(context, "claim")))))
            .then(Commands.literal("info")
                .executes(context -> getInfo(context.getSource())))
            .then(Commands.literal("list")
                .executes(context -> listClaims(context.getSource())))
            .then(Commands.literal("clear")
                .executes(context -> clearClaims(context.getSource())))
            .then(Commands.literal("show")
                .executes(context -> toggleVisuals(context.getSource())))
            .then(Commands.literal("trust")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("level", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (ClaimData.PermissionLevel level : ClaimData.PermissionLevel.values()) {
                                builder.suggest(level.name());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> trustPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "level"))))))
            .then(Commands.literal("untrust")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> untrustPlayer(context.getSource(), EntityArgument.getPlayer(context, "player")))))
            .then(Commands.literal("admin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("unclaim")
                    .executes(context -> unclaimChunk(context.getSource(), true)))
                .then(Commands.literal("reload")
                    .executes(context -> reloadConfig(context.getSource()))))
        );
    }

    private static int claimChunk(CommandSourceStack source, String name) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!ModConfig.USE_COMMANDS_FOR_CLAIM.get()) {
                source.sendFailure(Component.literal("Command-based claiming is disabled on this server. Use a Land Permit.")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            ChunkPos pos = player.chunkPosition();
            if (ClaimManager.getInstance().claim(player.level(), pos, player.getUUID(), player.getScoreboardName(), name)) {
                source.sendSuccess(() -> Component.literal("Successfully claimed this chunk!")
                        .withStyle(ChatFormatting.GREEN), false);
            } else {
                String error = "Failed to claim.";
                if (ModConfig.REQUIRE_NAME_ON_CLAIM.get() && name == null) {
                    error = "You must provide a name for your claim! Use /claim <name>";
                } else {
                    error = "Failed to claim (already claimed or limit reached).";
                }
                source.sendFailure(Component.literal(error).withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int renameClaim(CommandSourceStack source, String newName) {
        if (source.getEntity() instanceof ServerPlayer player) {
            // Check for permit item
            ItemStack permit = player.getInventory().items.stream()
                    .filter(stack -> stack.is(Claim.LAND_PERMIT.get()))
                    .findFirst().orElse(ItemStack.EMPTY);

            if (permit.isEmpty() && !player.hasPermissions(2)) {
                source.sendFailure(Component.literal("You need a Land Permit to rename your claim!")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            if (ClaimManager.getInstance().renameClaim(player.level(), player.chunkPosition(), player.getUUID(), newName)) {
                if (!player.hasPermissions(2)) permit.shrink(1); // Consume permit
                source.sendSuccess(() -> Component.literal("Claim renamed to: ")
                        .append(Component.literal(newName).withStyle(ChatFormatting.GOLD)), false);
            } else {
                source.sendFailure(Component.literal("Failed to rename. You must be the owner, and the name may already be taken.")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int visitClaim(CommandSourceStack source, String name) {
        if (source.getEntity() instanceof ServerPlayer player) {
            Optional<ClaimManager.ClaimTarget> target = ClaimManager.getInstance().findClaimByName(name);
            if (target.isPresent()) {
                TeleportManager.scheduleTeleport(player, target.get());
            } else {
                source.sendFailure(Component.literal("No claim found with that name.")
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
                String ownerDisplay = data.getOwnerName();
                String nameDisplay = data.getName() != null ? " (" + data.getName() + ")" : "";
                source.sendSuccess(() -> Component.literal("Owner: ")
                        .append(Component.literal(ownerDisplay + nameDisplay).withStyle(ChatFormatting.GOLD)), false);
            } else {
                source.sendSuccess(() -> Component.literal("This chunk is not claimed.")
                        .withStyle(ChatFormatting.GRAY), false);
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

    private static int trustPlayer(CommandSourceStack source, ServerPlayer target, String levelStr) {
        if (source.getEntity() instanceof ServerPlayer player) {
            try {
                ClaimData.PermissionLevel level = ClaimData.PermissionLevel.valueOf(levelStr.toUpperCase());
                ChunkPos pos = player.chunkPosition();
                ClaimData data = ClaimManager.getInstance().getClaim(player.level(), pos);
                if (data != null && data.getOwnerUUID().equals(player.getUUID())) {
                    data.trust(target.getUUID(), level);
                    ClaimManager.getInstance().save();
                    source.sendSuccess(() -> Component.literal("Successfully trusted ")
                            .append(Component.literal(target.getScoreboardName()).withStyle(ChatFormatting.GOLD))
                            .append(" with level: " + level.name()), false);
                } else {
                    source.sendFailure(Component.literal("You must stand in your own claim to trust someone!")
                            .withStyle(ChatFormatting.RED));
                }
            } catch (IllegalArgumentException e) {
                source.sendFailure(Component.literal("Invalid permission level! Use BUILD, INTERACT, CONTAINERS, or MANAGE.")
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
                boolean removed = false;
                if (data.getTrustedPlayers().containsKey(target.getUUID())) {
                    data.untrust(target.getUUID());
                    ClaimManager.getInstance().save();
                    removed = true;
                }
                
                if (removed) {
                    source.sendSuccess(() -> Component.literal("Successfully untrusted ")
                            .append(Component.literal(target.getScoreboardName()).withStyle(ChatFormatting.GOLD)), false);
                } else {
                    source.sendFailure(Component.literal("That player is not trusted here.")
                            .withStyle(ChatFormatting.RED));
                }
            } else {
                source.sendFailure(Component.literal("You must stand in your own claim to untrust someone!")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int clearClaims(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            int count = ClaimManager.getInstance().clearPlayerClaims(player.level(), player.getUUID());
            source.sendSuccess(() -> Component.literal("Cleared ")
                    .append(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.GOLD))
                    .append(" claims."), false);
        }
        return 1;
    }

    private static int toggleVisuals(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            // This is a client-side thing, so we notify the player
            source.sendSuccess(() -> Component.literal("Visualization toggled. (Use Surveyor's Compass for persistent visuals)")
                    .withStyle(ChatFormatting.AQUA), false);
        }
        return 1;
    }

    private static int reloadConfig(CommandSourceStack source) {
        // Forge config reloads automatically on file change, but we can log it
        source.sendSuccess(() -> Component.literal("Configuration is managed by Forge and reloads on change.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}

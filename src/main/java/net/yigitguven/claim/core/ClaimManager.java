package net.yigitguven.claim.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yigitguven.claim.Claim;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<ClaimData> claims = new ArrayList<>();
    private static int nextClaimId = 0;

    public static boolean addClaim(ServerLevel level, String displayName, UUID ownerUUID, BlockPos pos1, BlockPos pos2)
    {
        BlockPos[] normalized = getNormalizedPositions(level, pos1, pos2);
        pos1 = normalized[0];
        pos2 = normalized[1];

        if (isAreaClaimed(pos1, pos2))
        {
            return false;
        }

        String ownerName = "Unknown";
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (player != null) ownerName = player.getScoreboardName();

        String name = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_NAME.get().replace("{player}", ownerName);
        String desc = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_DESCRIPTION.get();
        ClaimData.PermissionMode mode = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_PERMISSIONS.get();
        int color = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_COLOR.get();

        claims.add(new ClaimData(nextClaimId++, name, ownerUUID, pos1, pos2, new ArrayList<>(), mode, System.currentTimeMillis(), color, desc));
        save(level);
        
        // Sync to all players
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
        return true;
    }

    public static boolean isAreaClaimed(BlockPos pos1, BlockPos pos2)
    {
        for (ClaimData claim : claims)
        {
            if (intersects(pos1, pos2, claim.pos1, claim.pos2))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean intersects(BlockPos a1, BlockPos a2, BlockPos b1, BlockPos b2)
    {
        return Math.min(a1.getX(), a2.getX()) <= Math.max(b1.getX(), b2.getX()) &&
               Math.max(a1.getX(), a2.getX()) >= Math.min(b1.getX(), b2.getX()) &&
               Math.min(a1.getY(), a2.getY()) <= Math.max(b1.getY(), b2.getY()) &&
               Math.max(a1.getY(), a2.getY()) >= Math.min(b1.getY(), b2.getY()) &&
               Math.min(a1.getZ(), a2.getZ()) <= Math.max(b1.getZ(), b2.getZ()) &&
               Math.max(a1.getZ(), a2.getZ()) >= Math.min(b1.getZ(), b2.getZ());
    }

    public static long calculateVolume(BlockPos p1, BlockPos p2)
    {
        long x = Math.abs(p1.getX() - p2.getX()) + 1;
        long y = Math.abs(p1.getY() - p2.getY()) + 1;
        long z = Math.abs(p1.getZ() - p2.getZ()) + 1;
        return x * y * z;
    }

    public static long calculateVolume(ServerLevel level, BlockPos p1, BlockPos p2)
    {
        BlockPos[] normalized = getNormalizedPositions(level, p1, p2);
        return calculateVolume(normalized[0], normalized[1]);
    }

    public static BlockPos[] getNormalizedPositions(ServerLevel level, BlockPos p1, BlockPos p2)
    {
        if (net.yigitguven.claim.config.ModConfig.CLAIM_ALL_Y.get())
        {
            return new BlockPos[]{
                new BlockPos(p1.getX(), level.getMinBuildHeight(), p1.getZ()),
                new BlockPos(p2.getX(), level.getMaxBuildHeight(), p2.getZ())
            };
        }
        return new BlockPos[]{p1, p2};
    }

    public static void removeClaims(ServerLevel level, List<Integer> claimIds, UUID playerUUID)
    {
        claims.removeIf(claim -> claimIds.contains(claim.claimId) && claim.ownerUUID.equals(playerUUID));
        save(level);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
    }

    public static boolean removeClaim(ServerLevel level, int claimId)
    {
        boolean removed = claims.removeIf(claim -> claim.claimId == claimId);
        if (removed)
        {
            save(level);
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
            {
                Claim.syncClaims(player);
            }
        }
        return removed;
    }

    public static void clearPlayerClaims(ServerLevel level, UUID playerUUID)
    {
        claims.removeIf(claim -> claim.ownerUUID.equals(playerUUID));
        save(level);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
    }

    public static void updateClaimMetadata(ServerLevel level, int claimId, String newName, String description, ClaimData.PermissionMode mode, int color, List<UUID> trustedPlayers, UUID ownerUUID)
    {
        for (ClaimData claim : claims)
        {
            if (claim.claimId == claimId && claim.ownerUUID.equals(ownerUUID))
            {
                claim.displayName = newName;
                claim.description = description;
                claim.permissionMode = mode;
                claim.color = color;
                claim.trustedPlayers = new ArrayList<>(trustedPlayers);
                
                save(level);
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
                {
                    Claim.syncClaims(player);
                }
                break;
            }
        }
    }

    public static ClaimData getClaimAt(BlockPos pos)
    {
        for (ClaimData claim : claims)
        {
            if (isInside(pos, claim.pos1, claim.pos2))
            {
                return claim;
            }
        }
        return null;
    }

    private static boolean isInside(BlockPos pos, BlockPos p1, BlockPos p2)
    {
        return pos.getX() >= Math.min(p1.getX(), p2.getX()) && pos.getX() <= Math.max(p1.getX(), p2.getX()) &&
               pos.getY() >= Math.min(p1.getY(), p2.getY()) && pos.getY() <= Math.max(p1.getY(), p2.getY()) &&
               pos.getZ() >= Math.min(p1.getZ(), p2.getZ()) && pos.getZ() <= Math.max(p1.getZ(), p2.getZ());
    }

    public static List<ClaimData> getClaims()
    {
        return new ArrayList<>(claims);
    }

    public static int getClaimCount(UUID playerUUID)
    {
        return (int) claims.stream().filter(claim -> claim.ownerUUID.equals(playerUUID)).count();
    }

    public static long getTotalClaimedBlocks(UUID playerUUID)
    {
        return claims.stream()
                .filter(claim -> claim.ownerUUID.equals(playerUUID))
                .mapToLong(ClaimData::getBlockCount)
                .sum();
    }

    public static void load(ServerLevel level)
    {
        File file = getSaveFile(level);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<ClaimData>>() {}.getType();
            List<ClaimData> loadedClaims = GSON.fromJson(reader, listType);
            if (loadedClaims != null) {
                claims.clear();
                claims.addAll(loadedClaims);
                nextClaimId = claims.stream().mapToInt(c -> c.claimId).max().orElse(-1) + 1;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load claims", e);
        }
    }

    public static void save(ServerLevel level)
    {
        File file = getSaveFile(level);
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(claims, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save claims", e);
        }
    }

    private static File getSaveFile(ServerLevel level)
    {
        return level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/claim/claims.json").toFile();
    }
}

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
        if (isAreaClaimed(pos1, pos2))
        {
            return false;
        }

        claims.add(new ClaimData(nextClaimId++, displayName, ownerUUID, pos1, pos2));
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

    public static void removeClaims(ServerLevel level, List<Integer> claimIds, UUID playerUUID)
    {
        claims.removeIf(claim -> claimIds.contains(claim.claimId) && claim.ownerUUID.equals(playerUUID));
        save(level);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
    }

    public static void renameClaim(ServerLevel level, int claimId, String newName, UUID ownerUUID)
    {
        for (ClaimData claim : claims)
        {
            if (claim.claimId == claimId && claim.ownerUUID.equals(ownerUUID))
            {
                claim.displayName = newName;
                save(level);
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
                {
                    Claim.syncClaims(player);
                }
                break;
            }
        }
    }

    public static List<ClaimData> getClaims()
    {
        return new ArrayList<>(claims);
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

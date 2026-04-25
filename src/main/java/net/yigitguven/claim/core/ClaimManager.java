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

    public static void addClaim(ServerLevel level, String displayName, UUID ownerUUID, BlockPos pos1, BlockPos pos2)
    {
        claims.add(new ClaimData(nextClaimId++, displayName, ownerUUID, pos1, pos2));
        save(level);
        
        // Sync to all players
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
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

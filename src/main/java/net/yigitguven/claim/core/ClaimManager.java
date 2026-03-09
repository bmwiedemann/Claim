package net.yigitguven.claim.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.yigitguven.claim.ModConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages all claims in the world.
 */
public class ClaimManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "claims.json";
    
    // Map of Dimension -> (ChunkPos -> ClaimData)
    private final Map<String, Map<Long, ClaimData>> claims = new ConcurrentHashMap<>();

    private static ClaimManager instance;

    public static ClaimManager getInstance() {
        if (instance == null) {
            instance = new ClaimManager();
        }
        return instance;
    }

    private ClaimManager() {}

    /**
     * Claims a chunk for a player.
     */
    public boolean claim(Level level, ChunkPos pos, UUID playerUUID, String playerName, String claimName) {
        if (ModConfig.REQUIRE_NAME_ON_CLAIM.get() && (claimName == null || claimName.isEmpty())) {
            return false;
        }

        String dimension = level.dimension().location().toString();
        Map<Long, ClaimData> dimensionClaims = claims.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        
        long chunkKey = pos.toLong();
        if (dimensionClaims.containsKey(chunkKey)) {
            return false; // Already claimed
        }

        // Enforcement of claim limits from config
        if (getPlayerClaimCount(level, playerUUID) >= ModConfig.MAX_CLAIMS.get()) {
             return false; // Limit reached
        }

        ClaimData newData = new ClaimData(playerUUID, playerName);
        if (claimName != null) newData.setName(claimName);
        dimensionClaims.put(chunkKey, newData);
        save();
        return true;
    }

    /**
     * Unclaims a chunk.
     */
    public boolean unclaim(Level level, ChunkPos pos, UUID playerUUID, boolean isAdmin) {
        String dimension = level.dimension().location().toString();
        Map<Long, ClaimData> dimensionClaims = claims.get(dimension);
        
        if (dimensionClaims == null) return false;

        long chunkKey = pos.toLong();
        ClaimData data = dimensionClaims.get(chunkKey);
        
        if (data == null) return false;

        if (isAdmin || data.getOwnerUUID().equals(playerUUID)) {
            dimensionClaims.remove(chunkKey);
            save();
            return true;
        }

        return false;
    }

    public int getPlayerClaimCount(Level level, UUID playerUUID) {
        String dimension = level.dimension().location().toString();
        Map<Long, ClaimData> dimensionClaims = claims.get(dimension);
        if (dimensionClaims == null) return 0;
        
        return (int) dimensionClaims.values().stream()
                .filter(data -> data.getOwnerUUID().equals(playerUUID))
                .count();
    }

    public int clearPlayerClaims(Level level, UUID playerUUID) {
        String dimension = level.dimension().location().toString();
        Map<Long, ClaimData> dimensionClaims = claims.get(dimension);
        if (dimensionClaims == null) return 0;

        int before = dimensionClaims.size();
        dimensionClaims.entrySet().removeIf(entry -> entry.getValue().getOwnerUUID().equals(playerUUID));
        int cleared = before - dimensionClaims.size();
        
        if (cleared > 0) save();
        return cleared;
    }

    public boolean renameClaim(Level level, ChunkPos pos, UUID playerUUID, String newName) {
        ClaimData data = getClaim(level, pos);
        if (data == null || !data.getOwnerUUID().equals(playerUUID)) return false;

        if (ModConfig.REQUIRE_UNIQUE_NAMES.get()) {
            Optional<ClaimData> existing = claims.values().stream()
                    .flatMap(m -> m.values().stream())
                    .filter(d -> d.getName() != null && 
                            (ModConfig.CASE_SENSITIVE_NAMES.get() ? 
                                    d.getName().equals(newName) : 
                                    d.getName().equalsIgnoreCase(newName)))
                    .findAny();
            
            if (existing.isPresent()) return false; // Name taken
        }

        data.setName(newName);
        save();
        return true;
    }

    public Optional<ClaimTarget> findClaimByName(String name) {
        for (Map.Entry<String, Map<Long, ClaimData>> dimensionEntry : claims.entrySet()) {
            for (Map.Entry<Long, ClaimData> claimEntry : dimensionEntry.getValue().entrySet()) {
                ClaimData data = claimEntry.getValue();
                if (data.getName() != null && 
                        (ModConfig.CASE_SENSITIVE_NAMES.get() ? 
                                data.getName().equals(name) : 
                                data.getName().equalsIgnoreCase(name))) {
                    return Optional.of(new ClaimTarget(dimensionEntry.getKey(), new ChunkPos(claimEntry.getKey())));
                }
            }
        }
        return Optional.empty();
    }

    public record ClaimTarget(String dimension, ChunkPos pos) {}

    public ClaimData getClaim(Level level, ChunkPos pos) {
        String dimension = level.dimension().location().toString();
        Map<Long, ClaimData> dimensionClaims = claims.get(dimension);
        if (dimensionClaims == null) return null;
        return dimensionClaims.get(pos.toLong());
    }

    public boolean isProtected(Level level, ChunkPos pos, UUID playerUUID, ClaimData.PermissionLevel levelRequired) {
        ClaimData data = getClaim(level, pos);
        if (data == null) return false; // Not claimed
        return !data.isTrusted(playerUUID, levelRequired);
    }

    /**
     * Saves claims to the world's data directory.
     */
    public void save() {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) return;
        
        Path dataPath = ServerLifecycleHooks.getCurrentServer().getWorldPath(LevelResource.ROOT).resolve("claimmod");
        File dir = dataPath.toFile();
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(claims, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save claims!", e);
        }
    }

    /**
     * Loads claims from the world's data directory.
     */
    public void load() {
        Path dataPath = ServerLifecycleHooks.getCurrentServer().getWorldPath(LevelResource.ROOT).resolve("claimmod");
        File file = dataPath.resolve(FILE_NAME).toFile();
        
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Map<Long, ClaimData>>>(){}.getType();
            Map<String, Map<Long, ClaimData>> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                claims.clear();
                claims.putAll(loaded);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load claims!", e);
        }
    }
}

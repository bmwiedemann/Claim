package net.yigitguven.claim.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yigitguven.claim.config.ModConfig;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerData> PLAYER_DATA_MAP = new HashMap<>();

    public static class PlayerData {
        public int claimBlocks;
        public long playtimeSeconds;

        public PlayerData(int claimBlocks) {
            this.claimBlocks = claimBlocks;
            this.playtimeSeconds = 0;
        }
    }

    public static PlayerData getOrCreateData(UUID uuid) {
        return PLAYER_DATA_MAP.computeIfAbsent(uuid, k -> new PlayerData(ModConfig.INITIAL_CLAIM_BLOCKS.get()));
    }

    public static void addClaimBlocks(UUID uuid, int amount) {
        if (!ModConfig.USE_CLAIM_BLOCKS.get()) return;
        PlayerData data = getOrCreateData(uuid);
        data.claimBlocks += amount;
    }

    public static boolean consumeClaimBlocks(UUID uuid, int amount) {
        if (!ModConfig.USE_CLAIM_BLOCKS.get()) return true;
        PlayerData data = getOrCreateData(uuid);
        if (data.claimBlocks >= amount) {
            data.claimBlocks -= amount;
            return true;
        }
        return false;
    }

    public static int getAvailableBlocks(UUID uuid) {
        if (!ModConfig.USE_CLAIM_BLOCKS.get()) return -1; // Indicator for "disabled"
        return getOrCreateData(uuid).claimBlocks;
    }

    public static void updatePlaytime(ServerPlayer player, int seconds) {
        if (!ModConfig.USE_CLAIM_BLOCKS.get()) return;
        
        PlayerData data = getOrCreateData(player.getUUID());
        data.playtimeSeconds += seconds;

        if (data.playtimeSeconds >= 3600) {
            int rewards = (int) (data.playtimeSeconds / 3600);
            int amount = rewards * ModConfig.HOURLY_REWARD.get();
            data.claimBlocks += amount;
            data.playtimeSeconds %= 3600;

            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aYou earned " + amount + " claim blocks for playing!"), false);
        }
    }

    public static void load(ServerLevel level) {
        File file = getSaveFile(level);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type mapType = new TypeToken<HashMap<UUID, PlayerData>>() {}.getType();
            Map<UUID, PlayerData> loadedData = GSON.fromJson(reader, mapType);
            if (loadedData != null) {
                PLAYER_DATA_MAP.clear();
                PLAYER_DATA_MAP.putAll(loadedData);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load player data", e);
        }
    }

    public static void save(ServerLevel level) {
        File file = getSaveFile(level);
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(PLAYER_DATA_MAP, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save player data", e);
        }
    }

    private static File getSaveFile(ServerLevel level) {
        return level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/claim/players.json").toFile();
    }
}

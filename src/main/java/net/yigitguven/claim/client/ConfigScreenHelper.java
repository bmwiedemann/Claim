package net.yigitguven.claim.client;

import net.minecraft.Util;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.yigitguven.claim.Claim;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.nio.file.Path;
import java.io.File;
import java.io.IOException;

public class ConfigScreenHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static long lastOpenTime = 0;

    public static void registerConfigScreen(net.neoforged.fml.ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, lastScreen) -> {
            // Simple debounce to prevent infinite recursion/loops
            long now = System.currentTimeMillis();
            if (now - lastOpenTime < 1000) {
                return lastScreen;
            }
            lastOpenTime = now;

            Path configPath = FMLPaths.CONFIGDIR.get().resolve(Claim.MODID + "-common.toml");
            File configFile = configPath.toFile();
            
            LOGGER.info("Config button clicked. Path: {}", configPath.toAbsolutePath());
            
            if (configFile.exists()) {
                try {
                    // Try to open using OS specific commands for better reliability in dev
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        new ProcessBuilder("explorer.exe", configFile.getAbsolutePath()).start();
                    } else if (os.contains("mac")) {
                        new ProcessBuilder("open", configFile.getAbsolutePath()).start();
                    } else {
                        Util.getPlatform().openFile(configFile);
                    }
                    LOGGER.info("Opened config file successfully.");
                } catch (IOException e) {
                    LOGGER.error("Failed to open config file via ProcessBuilder, falling back to Util", e);
                    Util.getPlatform().openFile(configFile);
                }
            } else {
                LOGGER.warn("Config file does not exist yet: {}", configPath.toAbsolutePath());
            }
            
            return lastScreen;
        });
    }
}

package net.yigitguven.claim;

import net.yigitguven.claim.commands.ClaimCommand;
import net.yigitguven.claim.core.ClaimManager;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Claim.MODID)
public class Claim
{
    public static final String MODID = "claim";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Claim(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the config
        modContainer.registerConfig(ModConfig.Type.COMMON, net.yigitguven.claim.config.ModConfig.SPEC);
        
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Claim mod server starting, loading claims...");
        ClaimManager.load(event.getServer().overworld());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        LOGGER.info("Claim mod server stopping, saving claims...");
        ClaimManager.save(event.getServer().overworld());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ClaimCommand.register(event.getDispatcher());
    }
}

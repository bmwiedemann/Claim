package net.yigitguven.claim;

import net.yigitguven.claim.commands.ClaimCommands;
import net.yigitguven.claim.core.ClaimManager;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import org.slf4j.Logger;

@Mod(Claim.MODID)
public class Claim
{
    public static final String MODID = "claim";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<Item> SURVEYOR_COMPASS = ITEMS.register("surveyor_compass", 
            () -> new SurveyorCompassItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LAND_PERMIT = ITEMS.register("land_permit", 
            () -> new LandPermitItem(new Item.Properties().stacksTo(16)));

    public Claim(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the items
        ITEMS.register(modEventBus);

        // Register the config
        modContainer.registerConfig(ModConfig.Type.COMMON, net.yigitguven.claim.ModConfig.SPEC);
        
        // Register the config screen factory (client only)
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            net.yigitguven.claim.client.ConfigScreenHelper.registerConfigScreen(modContainer);
        }

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Claim mod server starting, loading claims...");
        ClaimManager.getInstance().load();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        LOGGER.info("Claim mod server stopping, saving claims...");
        ClaimManager.getInstance().save();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ClaimCommands.register(event.getDispatcher());
    }
}

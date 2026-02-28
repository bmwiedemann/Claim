package net.yigitguven.claim;

import net.yigitguven.claim.commands.ClaimCommands;
import net.yigitguven.claim.core.ClaimManager;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Claim.MODID)
public class Claim
{
    public static final String MODID = "claim";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> SURVEYOR_COMPASS = ITEMS.register("surveyor_compass", 
            () -> new SurveyorCompassItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LAND_PERMIT = ITEMS.register("land_permit", 
            () -> new LandPermitItem(new Item.Properties().stacksTo(16)));

    public Claim(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        
        // Register the items
        ITEMS.register(modEventBus);

        // Register the config
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
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

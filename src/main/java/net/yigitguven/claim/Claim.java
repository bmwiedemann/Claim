package net.yigitguven.claim;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Claim.MODID)
public class Claim
{
    public static final String MODID = "claim";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Claim(IEventBus modEventBus, ModContainer modContainer)
    {
        LOGGER.info("Claim mod initializing...");
    }
}

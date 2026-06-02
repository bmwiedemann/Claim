package net.yigitguven.claim.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.yigitguven.claim.Claim;

@EventBusSubscriber(modid = Claim.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModItems
{
    public static final ResourceLocation LAND_PERMIT_ID = ResourceLocation.fromNamespaceAndPath(Claim.MODID, "land_permit");
    public static final ResourceLocation CLAIM_COMPASS_ID = ResourceLocation.fromNamespaceAndPath(Claim.MODID, "claim_compass");

    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event)
    {
        event.register(BuiltInRegistries.ITEM.key(), helper -> {
            helper.register(LAND_PERMIT_ID, new Item(new Item.Properties()));
            helper.register(CLAIM_COMPASS_ID, new Item(new Item.Properties().stacksTo(1)));
        });
    }
}

package net.yigitguven.claim.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> SELECTION_TOOL = BUILDER
            .comment("The item used to select claim positions. Format: 'modid:itemid'")
            .define("selectionTool", "minecraft:wooden_shovel");

    public static final ModConfigSpec SPEC = BUILDER.build();
}

package net.yigitguven.claim.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig
{
    public static final ModConfigSpec SPEC;
    
    // General Category
    public static final ModConfigSpec.ConfigValue<String> SELECTION_TOOL;
    public static final ModConfigSpec.BooleanValue OP_BYPASS;

    // Economy Category
    public static final ModConfigSpec.BooleanValue USE_CLAIM_BLOCKS;
    public static final ModConfigSpec.IntValue INITIAL_CLAIM_BLOCKS;
    public static final ModConfigSpec.IntValue HOURLY_REWARD;

    static {
        ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        BUILDER.push("general");
        SELECTION_TOOL = BUILDER
                .comment("The item used to select claim positions. Format: 'modid:itemid'")
                .define("selectionTool", "minecraft:wooden_shovel");

        OP_BYPASS = BUILDER
                .comment("Whether Operators (OPs) should bypass claim protections.")
                .define("opBypass", true);
        BUILDER.pop();

        BUILDER.push("economy");
        USE_CLAIM_BLOCKS = BUILDER
                .comment("Whether to use the 'Claim Block' economy system (blocks earned by play time).")
                .define("useClaimBlocks", false);

        INITIAL_CLAIM_BLOCKS = BUILDER
                .comment("The amount of claim blocks a player starts with. Only used if useClaimBlocks is true.")
                .defineInRange("initialClaimBlocks", 500, 0, Integer.MAX_VALUE);

        HOURLY_REWARD = BUILDER
                .comment("The amount of claim blocks a player earns every hour of play time. Only used if useClaimBlocks is true.")
                .defineInRange("hourlyReward", 100, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}

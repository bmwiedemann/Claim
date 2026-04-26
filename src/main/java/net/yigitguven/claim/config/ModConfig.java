package net.yigitguven.claim.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig
{
    public static final ModConfigSpec SPEC;
    
    // General Category
    public static final ModConfigSpec.ConfigValue<String> SELECTION_TOOL;
    public static final ModConfigSpec.BooleanValue OP_BYPASS;
    public static final ModConfigSpec.BooleanValue REQUIRE_ADJACENCY;

    // Economy Category
    public static final ModConfigSpec.BooleanValue USE_CLAIM_BLOCKS;
    public static final ModConfigSpec.IntValue INITIAL_CLAIM_BLOCKS;
    public static final ModConfigSpec.IntValue HOURLY_REWARD;
    public static final ModConfigSpec.ConfigValue<String> CLAIM_BLOCK_ITEM;
    public static final ModConfigSpec.IntValue BLOCKS_PER_ITEM;
    public static final ModConfigSpec.IntValue MAX_TOTAL_BLOCKS;

    static {
        ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        BUILDER.push("general");
        SELECTION_TOOL = BUILDER
                .comment("The item used to select claim positions. Format: 'modid:itemid'")
                .define("selectionTool", "minecraft:wooden_shovel");

        OP_BYPASS = BUILDER
                .comment("Whether Operators (OPs) should bypass claim protections.")
                .define("opBypass", true);

        REQUIRE_ADJACENCY = BUILDER
                .comment("If true, new claims must be adjacent to an existing claim owned by the player (after the first claim).")
                .define("requireAdjacency", false);
        BUILDER.pop();

        BUILDER.push("economy");
        USE_CLAIM_BLOCKS = BUILDER
                .comment("Whether to use the 'Claim Block' economy system (blocks earned by play time or items).")
                .define("useClaimBlocks", false);

        INITIAL_CLAIM_BLOCKS = BUILDER
                .comment("The amount of claim blocks a player starts with. Only used if useClaimBlocks is true.")
                .defineInRange("initialClaimBlocks", 500, 0, Integer.MAX_VALUE);

        HOURLY_REWARD = BUILDER
                .comment("The amount of claim blocks a player earns every hour of play time. Only used if useClaimBlocks is true.")
                .defineInRange("hourlyReward", 100, 0, Integer.MAX_VALUE);

        CLAIM_BLOCK_ITEM = BUILDER
                .comment("The item that can be consumed to earn more claim blocks. Format: 'modid:itemid'. Set to 'none' to disable.")
                .define("claimBlockItem", "minecraft:gold_ingot");

        BLOCKS_PER_ITEM = BUILDER
                .comment("How many claim blocks are granted per item consumed.")
                .defineInRange("blocksPerItem", 100, 1, Integer.MAX_VALUE);

        MAX_TOTAL_BLOCKS = BUILDER
                .comment("The maximum total claim blocks a player can accumulate. Set to 0 for unlimited.")
                .defineInRange("maxTotalBlocks", 10000, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}

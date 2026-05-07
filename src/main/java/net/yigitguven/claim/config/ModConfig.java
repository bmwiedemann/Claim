package net.yigitguven.claim.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> SELECTION_TOOL = BUILDER
            .comment("The item used to select claim positions. Format: 'modid:itemid'")
            .define("selectionTool", "minecraft:wooden_shovel");

    public static final ModConfigSpec.BooleanValue OP_BYPASS = BUILDER
            .comment("Whether Operators (OPs) should bypass claim protections.")
            .define("opBypass", true);

    public static final ModConfigSpec.IntValue MAX_CLAIMS = BUILDER
            .comment("The maximum number of claims a player can have. Set to -1 for unlimited.")
            .defineInRange("maxClaims", -1, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec.LongValue MAX_BLOCKS_PER_CLAIM = BUILDER
            .comment("The maximum number of blocks a single claim can cover. Set to -1 for unlimited.")
            .defineInRange("maxBlocksPerClaim", -1L, -1, Long.MAX_VALUE);

    public static final ModConfigSpec.LongValue MAX_TOTAL_BLOCKS = BUILDER
            .comment("The total maximum number of blocks a player can claim across all their claims. Set to -1 for unlimited.")
            .defineInRange("maxTotalBlocks", -1L, -1, Long.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue SHOW_CLAIM_ACTION_BAR = BUILDER
            .comment("Show a message in the action bar when entering/leaving a claim.")
            .define("showClaimActionBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_CLAIM_TITLE = BUILDER
            .comment("Show a title on screen when entering a claim.")
            .define("showClaimTitle", false);

    public static final ModConfigSpec.BooleanValue SHOW_CLAIM_CHAT = BUILDER
            .comment("Show a message in chat when entering/leaving a claim.")
            .define("showClaimChat", false);

    public static final ModConfigSpec.BooleanValue CLAIM_ALL_Y = BUILDER
            .comment("Whether claims should automatically cover all Y levels (from bedrock to sky).")
            .define("claimAllY", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}

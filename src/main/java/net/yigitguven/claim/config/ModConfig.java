package net.yigitguven.claim.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> SELECTION_TOOL;
    public static final ModConfigSpec.BooleanValue OP_BYPASS;
    public static final ModConfigSpec.BooleanValue CLAIM_ALL_Y;
        public static final ModConfigSpec.BooleanValue REQUIRE_LAND_PERMIT;
        public static final ModConfigSpec.ConfigValue<String> LAND_PERMIT_ITEM;
        public static final ModConfigSpec.IntValue LAND_PERMIT_AMOUNT;
        public static final ModConfigSpec.BooleanValue CONSUME_LAND_PERMIT_ON_USE;

    public static final ModConfigSpec.IntValue MAX_CLAIMS;
    public static final ModConfigSpec.LongValue MAX_BLOCKS_PER_CLAIM;
    public static final ModConfigSpec.LongValue MAX_TOTAL_BLOCKS;

    public static final ModConfigSpec.BooleanValue SHOW_CLAIM_ACTION_BAR;
    public static final ModConfigSpec.BooleanValue SHOW_CLAIM_TITLE;
    public static final ModConfigSpec.BooleanValue SHOW_CLAIM_CHAT;
        public static final ModConfigSpec.BooleanValue SUPPRESS_SAME_OWNER_NAME_TRANSITIONS;

        public static final ModConfigSpec.BooleanValue VISIT_OWNER_ONLY;

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_CLAIM_NAME;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_CLAIM_DESCRIPTION;
    public static final ModConfigSpec.EnumValue<net.yigitguven.claim.core.ClaimData.PermissionMode> DEFAULT_CLAIM_PERMISSIONS;
    public static final ModConfigSpec.IntValue DEFAULT_CLAIM_COLOR;

    public static final ModConfigSpec.BooleanValue LOCK_CLAIM_NAME;
    public static final ModConfigSpec.BooleanValue LOCK_CLAIM_DESCRIPTION;
    public static final ModConfigSpec.BooleanValue LOCK_CLAIM_PERMISSIONS;
    public static final ModConfigSpec.BooleanValue LOCK_CLAIM_COLOR;
    public static final ModConfigSpec.BooleanValue LOCK_CLAIM_TRUSTED;

    static {
        BUILDER.push("General");
        SELECTION_TOOL = BUILDER
                .comment("The item used to select claim positions. Format: 'modid:itemid'")
                .define("selectionTool", "minecraft:wooden_shovel");

        OP_BYPASS = BUILDER
                .comment("Whether Operators (OPs) should bypass claim protections and limits.")
                .define("opBypass", true);

        CLAIM_ALL_Y = BUILDER
                .comment("Whether claims should automatically cover all Y levels (from bedrock to sky).")
                .define("claimAllY", true);

        REQUIRE_LAND_PERMIT = BUILDER
                .comment("Whether players must have a permit item to create claims.")
                .define("requireLandPermit", false);

        LAND_PERMIT_ITEM = BUILDER
                .comment("Permit item id used for claim creation when requireLandPermit is enabled.")
                .define("landPermitItem", "minecraft:paper");

        LAND_PERMIT_AMOUNT = BUILDER
                .comment("How many permit items are required per claim.")
                .defineInRange("landPermitAmount", 1, 1, Integer.MAX_VALUE);

        CONSUME_LAND_PERMIT_ON_USE = BUILDER
                .comment("Whether permit items are consumed when a claim is successfully created.")
                .define("consumeLandPermitOnUse", false);
        BUILDER.pop();

        BUILDER.push("Limits");
        MAX_CLAIMS = BUILDER
                .comment("The maximum number of claims a player can have. Set to -1 for unlimited.")
                .defineInRange("maxClaims", -1, -1, Integer.MAX_VALUE);

        MAX_BLOCKS_PER_CLAIM = BUILDER
                .comment("The maximum number of blocks a single claim can cover. Set to -1 for unlimited.")
                .defineInRange("maxBlocksPerClaim", -1L, -1, Long.MAX_VALUE);

        MAX_TOTAL_BLOCKS = BUILDER
                .comment("The total maximum number of blocks a player can claim across all their claims. Set to -1 for unlimited.")
                .defineInRange("maxTotalBlocks", -1L, -1, Long.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Indicators");
        SHOW_CLAIM_ACTION_BAR = BUILDER
                .comment("Show a message in the action bar when entering/leaving a claim.")
                .define("showClaimActionBar", true);

        SHOW_CLAIM_TITLE = BUILDER
                .comment("Show a title on screen when entering a claim.")
                .define("showClaimTitle", false);

        SHOW_CLAIM_CHAT = BUILDER
                .comment("Show a message in chat when entering/leaving a claim.")
                .define("showClaimChat", false);

        SUPPRESS_SAME_OWNER_NAME_TRANSITIONS = BUILDER
                .comment("Suppress enter/leave indicators when moving between adjacent claims with the same owner and name.")
                .define("suppressSameOwnerNameTransitions", true);
        BUILDER.pop();

        BUILDER.push("Visit");
        VISIT_OWNER_ONLY = BUILDER
                .comment("Allow only claim owners to use /claim visit on their claims.")
                .define("visitOwnerOnly", false);
        BUILDER.pop();

        BUILDER.push("Defaults");
        DEFAULT_CLAIM_NAME = BUILDER
                .comment("The default name for new claims. Use {player} for the owner's name.")
                .define("defaultClaimName", "{player}'s Claim");

        DEFAULT_CLAIM_DESCRIPTION = BUILDER
                .comment("The default description for new claims.")
                .define("defaultClaimDescription", "");

        DEFAULT_CLAIM_PERMISSIONS = BUILDER
                .comment("The default permission mode for new claims.")
                .defineEnum("defaultClaimPermissions", net.yigitguven.claim.core.ClaimData.PermissionMode.PRIVATE);

        DEFAULT_CLAIM_COLOR = BUILDER
                .comment("The default color for new claims (Decimal ARGB).")
                .defineInRange("defaultClaimColor", 0xFF55FF7D, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Locks");
        LOCK_CLAIM_NAME = BUILDER
                .comment("Lock the claim name setting so players cannot change it.")
                .define("lockClaimName", false);

        LOCK_CLAIM_DESCRIPTION = BUILDER
                .comment("Lock the claim description setting so players cannot change it.")
                .define("lockClaimDescription", false);

        LOCK_CLAIM_PERMISSIONS = BUILDER
                .comment("Lock the claim permissions (Public/Private) setting so players cannot change it.")
                .define("lockClaimPermissions", false);

        LOCK_CLAIM_COLOR = BUILDER
                .comment("Lock the claim color setting so players cannot change it.")
                .define("lockClaimColor", false);

        LOCK_CLAIM_TRUSTED = BUILDER
                .comment("Lock the claim trusted players setting so players cannot change it.")
                .define("lockClaimTrusted", false);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}

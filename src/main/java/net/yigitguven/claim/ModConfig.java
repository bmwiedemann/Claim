package net.yigitguven.claim;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Claim.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_CLAIMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PROTECT_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PROTECT_INTERACT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PROTECT_EXPLOSIONS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> USE_ITEMS_FOR_CLAIM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> USE_COMMANDS_FOR_CLAIM;
    
    public static final ForgeConfigSpec.ConfigValue<Boolean> REQUIRE_UNIQUE_NAMES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CASE_SENSITIVE_NAMES;
    
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_VISIT;
    public static final ForgeConfigSpec.ConfigValue<Integer> TELEPORT_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CANCEL_TP_ON_MOVE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CANCEL_TP_ON_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_ACTION_BAR_NOTIFICATIONS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> REQUIRE_NAME_ON_CLAIM;

    static {
        BUILDER.push("Claim Mod Settings");

        MAX_CLAIMS = BUILDER.comment("Maximum chunks a player can claim")
                .defineInRange("maxClaims", 10, 1, 1000);

        ENABLE_ACTION_BAR_NOTIFICATIONS = BUILDER.comment("Show action bar messages when entering/leaving claims")
                .define("enableActionBarNotifications", true);

        REQUIRE_NAME_ON_CLAIM = BUILDER.comment("Force players to name their claim during the claiming process")
                .define("requireNameOnClaim", false);

        PROTECT_BLOCKS = BUILDER
                .comment("Whether to prevent block breaking and placing in claimed chunks. Default: true")
                .define("protectBlocks", true);

        PROTECT_INTERACT = BUILDER
                .comment("Whether to prevent block interactions (chests, doors) in claimed chunks. Default: true")
                .define("protectInteract", true);

        PROTECT_EXPLOSIONS = BUILDER
                .comment("Whether to prevent explosions from damaging claimed chunks. Default: true")
                .define("protectExplosions", true);

        BUILDER.push("Claiming Methods");
        USE_ITEMS_FOR_CLAIM = BUILDER.comment("Allow players to claim using items (surveyor wand).").define("useItems", true);
        USE_COMMANDS_FOR_CLAIM = BUILDER.comment("Allow players to claim using commands.").define("useCommands", true);
        BUILDER.pop();

        BUILDER.push("Naming & Visiting");
        REQUIRE_UNIQUE_NAMES = BUILDER.comment("Whether claim names must be unique globally.").define("uniqueNames", true);
        CASE_SENSITIVE_NAMES = BUILDER.comment("Whether name uniqueness check is case-sensitive.").define("caseSensitive", false);
        ENABLE_VISIT = BUILDER.comment("Enable the /visit command.").define("enableVisit", true);
        TELEPORT_COOLDOWN = BUILDER.comment("Seconds to wait before teleporting.").defineInRange("tpCooldown", 3, 0, 60);
        CANCEL_TP_ON_MOVE = BUILDER.comment("Cancel teleport if the player moves.").define("cancelOnMove", true);
        CANCEL_TP_ON_DAMAGE = BUILDER.comment("Cancel teleport if the player takes damage.").define("cancelOnDamage", true);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

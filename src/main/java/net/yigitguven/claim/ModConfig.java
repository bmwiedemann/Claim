package net.yigitguven.claim;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> MAX_CLAIMS;
    public static final ModConfigSpec.ConfigValue<Boolean> PROTECT_BLOCKS;
    public static final ModConfigSpec.ConfigValue<Boolean> PROTECT_INTERACT;
    public static final ModConfigSpec.ConfigValue<Boolean> PROTECT_EXPLOSIONS;

    public static final ModConfigSpec.ConfigValue<Boolean> USE_ITEMS_FOR_CLAIM;
    public static final ModConfigSpec.ConfigValue<Boolean> USE_COMMANDS_FOR_CLAIM;
    
    public static final ModConfigSpec.ConfigValue<Boolean> REQUIRE_UNIQUE_NAMES;
    public static final ModConfigSpec.ConfigValue<Boolean> CASE_SENSITIVE_NAMES;
    
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_VISIT;
    public static final ModConfigSpec.ConfigValue<Integer> TELEPORT_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Boolean> CANCEL_TP_ON_MOVE;
    public static final ModConfigSpec.ConfigValue<Boolean> CANCEL_TP_ON_DAMAGE;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_ACTION_BAR_NOTIFICATIONS;
    public static final ModConfigSpec.ConfigValue<Boolean> REQUIRE_NAME_ON_CLAIM;

    static {
        BUILDER.push("Claim Mod Settings");

        MAX_CLAIMS = BUILDER.comment("Maximum number of chunks a player can claim per dimension (e.g., Overworld, Nether, End are counted separately).")
                .defineInRange("maxClaims", 10, 1, 1000);

        ENABLE_ACTION_BAR_NOTIFICATIONS = BUILDER.comment("If true, players will see 'Entering: [Claim Name]' or 'Leaving: [Claim Name]' messages in their action bar when crossing borders.")
                .define("enableActionBarNotifications", true);

        REQUIRE_NAME_ON_CLAIM = BUILDER.comment("If true, players cannot claim a chunk unless they provide a name (e.g., /claim MyHome).")
                .define("requireNameOnClaim", false);

        PROTECT_BLOCKS = BUILDER
                .comment("If true, players without 'BUILD' trust cannot break or place blocks in claimed territories.")
                .define("protectBlocks", true);

        PROTECT_INTERACT = BUILDER
                .comment("If true, players without sufficient trust cannot interact with blocks (doors, buttons) or open containers (chests, hoppers).")
                .define("protectInteract", true);

        PROTECT_EXPLOSIONS = BUILDER
                .comment("If true, blocks within claimed territories are immune to all explosion damage (Creeper, TNT, Fireball, etc.).")
                .define("protectExplosions", true);

        BUILDER.push("Claiming Methods");
        USE_ITEMS_FOR_CLAIM = BUILDER.comment("If true, players can use 'Land Permits' to claim and rename chunks.").define("useItems", true);
        USE_COMMANDS_FOR_CLAIM = BUILDER.comment("If true, players can use the /claim command to secure territory without needing items.").define("useCommands", true);
        BUILDER.pop();

        BUILDER.push("Naming & Visiting");
        REQUIRE_UNIQUE_NAMES = BUILDER.comment("If true, every named claim on the server must have a unique name, regardless of dimension or owner.").define("uniqueNames", true);
        CASE_SENSITIVE_NAMES = BUILDER.comment("If true, 'MyBase' and 'mybase' will be considered different names for uniqueness checks.").define("caseSensitive", false);
        ENABLE_VISIT = BUILDER.comment("If true, players can use /claim visit <name> to teleport to named claims (subject to cooldown).").define("enableVisit", true);
        TELEPORT_COOLDOWN = BUILDER.comment("The delay in seconds a player must wait (without moving or taking damage) before being teleported to a claim.").defineInRange("tpCooldown", 3, 0, 60);
        CANCEL_TP_ON_MOVE = BUILDER.comment("If true, moving any distance while the teleport timer is active will cancel the teleportation.").define("cancelOnMove", true);
        CANCEL_TP_ON_DAMAGE = BUILDER.comment("If true, taking any damage while the teleport timer is active will cancel the teleportation.").define("cancelOnDamage", true);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

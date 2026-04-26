package net.yigitguven.claim;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClaimConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue OP_BYPASS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        OP_BYPASS = builder.comment("Whether Operators (OPs) should bypass claim protections.")
                .define("opBypass", true);
        builder.pop();
        SPEC = builder.build();
    }
}

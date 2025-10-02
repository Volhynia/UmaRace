// --- FILE_PATH: moe/gensoukyo/umarace/Config.java ---
package moe.gensoukyo.umarace;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 在这里为你的模组添加配置项
    // 例如: public static final ModConfigSpec.IntValue RACE_MAX_PARTICIPANTS = BUILDER.defineInRange("raceMaxParticipants", 18, 2, 18);

    static final ModConfigSpec SPEC = BUILDER.build();
}
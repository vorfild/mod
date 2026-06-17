package com.warfield.cursedfood;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Конфигурация мода (NeoForge TOML-конфиг через ModConfigSpec).
 * Файл создаётся в saves/<мир>/serverconfig/cursedfood-server.toml.
 */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Если true — добавлять запись в бан-лист и кикать. Если false — только кик. */
    public static final ModConfigSpec.BooleanValue BAN_ENABLED;

    /** Причина бана/кика, отображаемая игроку на экране отключения. */
    public static final ModConfigSpec.ConfigValue<String> BAN_REASON;

    static {
        BUILDER.comment("Настройки CursedFood").push("general");

        BAN_ENABLED = BUILDER
                .comment("true — банить игрока навсегда; false — только кик без бана")
                .define("ban_enabled", true);

        BAN_REASON = BUILDER
                .comment("Текст причины бана/кика, видимый игроку при отключении")
                .define("ban_reason", "Съел проклятую еду");

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}

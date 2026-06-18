package com.warfield.tankmod;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Конфигурация мода (серверная сторона). */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue  MAX_SPEED;
    public static final ModConfigSpec.IntValue     RELOAD_TICKS;
    public static final ModConfigSpec.IntValue     SHELL_CAPACITY;
    public static final ModConfigSpec.DoubleValue  SHELL_DAMAGE;
    public static final ModConfigSpec.DoubleValue  EXPLOSION_RADIUS;
    public static final ModConfigSpec.BooleanValue DESTROY_BLOCKS;
    public static final ModConfigSpec.DoubleValue  TANK_HEALTH;

    static {
        BUILDER.comment("Настройки TankMod").push("tank");

        MAX_SPEED = BUILDER
                .comment("Максимальная скорость танка (блоков/тик)")
                .defineInRange("max_speed", 0.28, 0.05, 1.0);

        RELOAD_TICKS = BUILDER
                .comment("Задержка перезарядки после выстрела (тики, 20 = 1 сек)")
                .defineInRange("reload_ticks", 60, 10, 400);

        SHELL_CAPACITY = BUILDER
                .comment("Максимальный боезапас в танке")
                .defineInRange("shell_capacity", 5, 1, 32);

        SHELL_DAMAGE = BUILDER
                .comment("Урон снаряда в эпицентре взрыва (сердечки × 2 = HP)")
                .defineInRange("shell_damage", 40.0, 1.0, 200.0);

        EXPLOSION_RADIUS = BUILDER
                .comment("Радиус взрыва снаряда (блоков)")
                .defineInRange("explosion_radius", 3.5, 0.5, 10.0);

        DESTROY_BLOCKS = BUILDER
                .comment("Разрушать ли блоки при взрыве снаряда")
                .define("destroy_blocks", false);

        TANK_HEALTH = BUILDER
                .comment("Начальное здоровье танка (очки урона; 1 heart = 2)")
                .defineInRange("tank_health", 200.0, 10.0, 2000.0);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}

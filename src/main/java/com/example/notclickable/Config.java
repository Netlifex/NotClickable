package com.example.notclickable;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = NotClickableMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.IntValue commandPermissionLevelRaw;

    private static int cachedPermissionLevel = 2;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        commandPermissionLevelRaw = builder
                .comment("Permission level required to use /notclickable command (0-4). Default: 2")
                .defineInRange("commandPermissionLevel", 2, 0, 4);
        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onModConfigEvent);
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    private static void onModConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_CONFIG) {
            cachedPermissionLevel = commandPermissionLevelRaw.get();
            System.out.println("[NotClickable] Config loaded. Command permission level = " + cachedPermissionLevel);
        }
    }

    /** Безопасно возвращает уровень доступа */
    public static int getCommandPermissionLevel() {
        try {
            return commandPermissionLevelRaw.get();
        } catch (IllegalStateException e) {
            return cachedPermissionLevel;
        }
    }
}

package me.purpleeast.mods.ppe_essential;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public class PpeConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final Map<String, CommandConfig> COMMANDS = new LinkedHashMap<>();

    public static final ModConfigSpec.ConfigValue<String> FALLBACK_LANGUAGE = BUILDER
            .comment("Fallback language used by the server when the player's client language is unsupported.",
                    "This prevents clients without the mod installed from seeing untranslated localization keys.",
                    "Supported values: en_us, zh_cn, zh_tw, zh_hk, ru_ru, de_de, fr_fr, ja_jp, ko_kr")
            .define("fallbackLanguage", "en_us", PpeConfig::isSupportedLanguage);

    public static final ModConfigSpec.BooleanValue FIRST_JOIN_NOTICE = BUILDER
            .comment("Whether PPE Essential should show a one-time command notice when a player first joins the server.")
            .define("firstJoinNotice", true);

    public static final ModConfigSpec.IntValue RTP_COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown in seconds for /rtp. Use 0 to disable the cooldown.")
            .defineInRange("rtpCooldownSeconds", 30, 0, 86400);

    public static final ModConfigSpec.IntValue RTP_MIN_DISTANCE = BUILDER
            .comment("Minimum distance from the current position for /rtp in normal dimensions.")
            .defineInRange("rtpMinDistance", 2000, 0, 30000000);

    public static final ModConfigSpec.IntValue RTP_MAX_DISTANCE = BUILDER
            .comment("Maximum distance from the current position for /rtp in normal dimensions.")
            .defineInRange("rtpMaxDistance", 5000, 1, 30000000);

    public static final ModConfigSpec.IntValue RTP_NETHER_MIN_DISTANCE = BUILDER
            .comment("Minimum distance from the current position for /rtp in the Nether.")
            .defineInRange("rtpNetherMinDistance", 600, 0, 30000000);

    public static final ModConfigSpec.IntValue RTP_NETHER_MAX_DISTANCE = BUILDER
            .comment("Maximum distance from the current position for /rtp in the Nether.")
            .defineInRange("rtpNetherMaxDistance", 1500, 1, 30000000);

    public static final ModConfigSpec.IntValue TELEPORT_REQUEST_TIMEOUT_SECONDS = BUILDER
            .comment("How long TPA and TPAhere requests stay valid, in seconds.")
            .defineInRange("teleportRequestTimeoutSeconds", 60, 1, 86400);

    static {
        BUILDER.push("commands");
        command("tpa", true, 0);
        command("tpaa", true, 0);
        command("tpad", true, 0);
        command("tpaauto", true, 0);
        command("tpahere", true, 0);
        command("tpaherea", true, 0);
        command("tpahered", true, 0);
        command("rtp", true, 0);
        command("spawn", true, 0);
        command("back", true, 0);
        command("dback", true, 0);
        command("tback", true, 0);
        command("sethome", true, 0);
        command("delhome", true, 0);
        command("home", true, 0);
        command("suicide", true, 0);
        command("trash", true, 0);
        command("ppe-essential", true, 0);
        command("ppe-essential-reset", true, 4);
        command("warp", true, 0);
        command("setwarp", true, 4);
        command("delwarp", true, 4);
        command("repeat", true, 4);
        command("heal", true, 4);
        command("fly", true, 4);
        command("god", true, 4);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean isSupportedLanguage(Object value) {
        return value instanceof String language && ("en_us".equals(language)
                || "zh_cn".equals(language)
                || "zh_tw".equals(language)
                || "zh_hk".equals(language)
                || "ru_ru".equals(language)
                || "de_de".equals(language)
                || "fr_fr".equals(language)
                || "ja_jp".equals(language)
                || "ko_kr".equals(language));
    }

    public static String fallbackLanguage() {
        return FALLBACK_LANGUAGE.get();
    }

    public static boolean firstJoinNotice() {
        return FIRST_JOIN_NOTICE.get();
    }

    public static int rtpCooldownSeconds() {
        return RTP_COOLDOWN_SECONDS.get();
    }

    public static int rtpMinDistance() {
        return RTP_MIN_DISTANCE.get();
    }

    public static int rtpMaxDistance() {
        return Math.max(RTP_MAX_DISTANCE.get(), rtpMinDistance() + 1);
    }

    public static int rtpNetherMinDistance() {
        return RTP_NETHER_MIN_DISTANCE.get();
    }

    public static int rtpNetherMaxDistance() {
        return Math.max(RTP_NETHER_MAX_DISTANCE.get(), rtpNetherMinDistance() + 1);
    }

    public static int teleportRequestTimeoutSeconds() {
        return TELEPORT_REQUEST_TIMEOUT_SECONDS.get();
    }

    public static boolean commandEnabled(String command) {
        CommandConfig config = COMMANDS.get(command);
        return config == null || config.enabled().get();
    }

    public static int commandPermission(String command) {
        CommandConfig config = COMMANDS.get(command);
        return config == null ? 0 : config.permissionLevel().get();
    }

    private static void command(String name, boolean enabled, int permissionLevel) {
        BUILDER.push(name);
        COMMANDS.put(name, new CommandConfig(
                BUILDER.comment("Whether /" + name + " is registered when the server starts. Changes require a server restart.")
                        .define("enabled", enabled),
                BUILDER.comment("Required OP permission level for /" + name + ". Use 0 to allow everyone. Changes apply without restart.")
                        .defineInRange("permissionLevel", permissionLevel, 0, 4)
        ));
        BUILDER.pop();
    }

    private record CommandConfig(ModConfigSpec.BooleanValue enabled, ModConfigSpec.IntValue permissionLevel) {
    }
}

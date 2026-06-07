package me.purpleeast.mods.ppe_essentials;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PpeConfig {
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "en_us", "zh_cn", "zh_tw", "zh_hk", "ru_ru", "de_de", "fr_fr", "ja_jp", "ko_kr"
    );
    private static final Map<String, CommandConfig> COMMANDS = new LinkedHashMap<>();

    private static String fallbackLanguage = "en_us";
    private static boolean firstJoinNotice = true;
    private static boolean preventCreeperBlockDamage = true;
    private static boolean preventEndermanBlockDamage = true;
    private static boolean preventRavagerBlockDamage = true;
    private static int rtpCooldownSeconds = 30;
    private static int rtpMinDistance = 2000;
    private static int rtpMaxDistance = 5000;
    private static int rtpNetherMinDistance = 600;
    private static int rtpNetherMaxDistance = 1500;
    private static int teleportRequestTimeoutSeconds = 60;
    private static boolean allowSelfTeleportRequests = true;
    private static long lastLoadedModifiedMillis = Long.MIN_VALUE;

    static {
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
        command("ppe-ess", true, 0);
        command("ppe-ess-reset", true, 4);
        command("warp", true, 0);
        command("setwarp", true, 4);
        command("delwarp", true, 4);
        command("repeat", true, 4);
        command("heal", true, 4);
        command("fly", true, 4);
        command("god", true, 4);
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("ppe_essentials-common.toml");
        if (!Files.exists(path)) {
            writeDefault(path);
            updateLastLoaded(path);
            return;
        }

        try {
            resetDefaults();
            parse(Files.readAllLines(path, StandardCharsets.UTF_8));
            updateLastLoaded(path);
        } catch (IOException exception) {
            PpeEssentials.LOGGER.warn("Failed to load PPE Essentials config: {}", path, exception);
        }
    }

    public static String fallbackLanguage() {
        reloadIfChanged();
        return fallbackLanguage;
    }

    public static boolean firstJoinNotice() {
        reloadIfChanged();
        return firstJoinNotice;
    }

    public static boolean preventCreeperBlockDamage() {
        reloadIfChanged();
        return preventCreeperBlockDamage;
    }

    public static boolean preventEndermanBlockDamage() {
        reloadIfChanged();
        return preventEndermanBlockDamage;
    }

    public static boolean preventRavagerBlockDamage() {
        reloadIfChanged();
        return preventRavagerBlockDamage;
    }

    public static int rtpCooldownSeconds() {
        reloadIfChanged();
        return rtpCooldownSeconds;
    }

    public static int rtpMinDistance() {
        reloadIfChanged();
        return rtpMinDistance;
    }

    public static int rtpMaxDistance() {
        reloadIfChanged();
        return Math.max(rtpMaxDistance, rtpMinDistance() + 1);
    }

    public static int rtpNetherMinDistance() {
        reloadIfChanged();
        return rtpNetherMinDistance;
    }

    public static int rtpNetherMaxDistance() {
        reloadIfChanged();
        return Math.max(rtpNetherMaxDistance, rtpNetherMinDistance() + 1);
    }

    public static int teleportRequestTimeoutSeconds() {
        reloadIfChanged();
        return teleportRequestTimeoutSeconds;
    }

    public static boolean allowSelfTeleportRequests() {
        reloadIfChanged();
        return allowSelfTeleportRequests;
    }

    public static boolean commandEnabled(String command) {
        reloadIfChanged();
        CommandConfig config = COMMANDS.get(command);
        return config == null || config.enabled();
    }

    public static int commandPermission(String command) {
        reloadIfChanged();
        CommandConfig config = COMMANDS.get(command);
        return config == null ? 0 : config.permissionLevel();
    }

    private static void reloadIfChanged() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("ppe_essentials-common.toml");
        if (!Files.exists(path)) {
            return;
        }

        try {
            long modifiedMillis = Files.getLastModifiedTime(path).toMillis();
            if (modifiedMillis != lastLoadedModifiedMillis) {
                load();
            }
        } catch (IOException exception) {
            PpeEssentials.LOGGER.warn("Failed to check PPE Essentials config timestamp: {}", path, exception);
        }
    }

    private static void updateLastLoaded(Path path) {
        try {
            lastLoadedModifiedMillis = Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : Long.MIN_VALUE;
        } catch (IOException exception) {
            lastLoadedModifiedMillis = Long.MIN_VALUE;
        }
    }

    private static void resetDefaults() {
        fallbackLanguage = "en_us";
        firstJoinNotice = true;
        preventCreeperBlockDamage = true;
        preventEndermanBlockDamage = true;
        preventRavagerBlockDamage = true;
        rtpCooldownSeconds = 30;
        rtpMinDistance = 2000;
        rtpMaxDistance = 5000;
        rtpNetherMinDistance = 600;
        rtpNetherMaxDistance = 1500;
        teleportRequestTimeoutSeconds = 60;
        allowSelfTeleportRequests = true;
        for (Map.Entry<String, CommandConfig> entry : COMMANDS.entrySet()) {
            entry.getValue().reset();
        }
    }

    private static void parse(Iterable<String> lines) {
        String section = "";
        for (String rawLine : lines) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
                continue;
            }

            int equals = line.indexOf('=');
            if (equals < 0) {
                continue;
            }

            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();
            if (section.startsWith("commands.")) {
                parseCommand(section.substring("commands.".length()), key, value);
            } else {
                parseRoot(key, value);
            }
        }
    }

    private static void parseRoot(String key, String value) {
        switch (key) {
            case "fallbackLanguage" -> {
                String language = parseString(value);
                if (SUPPORTED_LANGUAGES.contains(language)) {
                    fallbackLanguage = language;
                }
            }
            case "firstJoinNotice" -> firstJoinNotice = parseBoolean(value, firstJoinNotice);
            case "preventCreeperBlockDamage" -> preventCreeperBlockDamage = parseBoolean(value, preventCreeperBlockDamage);
            case "preventEndermanBlockDamage" -> preventEndermanBlockDamage = parseBoolean(value, preventEndermanBlockDamage);
            case "preventRavagerBlockDamage" -> preventRavagerBlockDamage = parseBoolean(value, preventRavagerBlockDamage);
            case "rtpCooldownSeconds" -> rtpCooldownSeconds = parseInt(value, rtpCooldownSeconds, 0, 86400);
            case "rtpMinDistance" -> rtpMinDistance = parseInt(value, rtpMinDistance, 0, 30000000);
            case "rtpMaxDistance" -> rtpMaxDistance = parseInt(value, rtpMaxDistance, 1, 30000000);
            case "rtpNetherMinDistance" -> rtpNetherMinDistance = parseInt(value, rtpNetherMinDistance, 0, 30000000);
            case "rtpNetherMaxDistance" -> rtpNetherMaxDistance = parseInt(value, rtpNetherMaxDistance, 1, 30000000);
            case "teleportRequestTimeoutSeconds" -> teleportRequestTimeoutSeconds = parseInt(value, teleportRequestTimeoutSeconds, 1, 86400);
            case "allowSelfTeleportRequests" -> allowSelfTeleportRequests = parseBoolean(value, allowSelfTeleportRequests);
            default -> {
            }
        }
    }

    private static void parseCommand(String command, String key, String value) {
        CommandConfig config = COMMANDS.get(command);
        if (config == null) {
            return;
        }

        if ("enabled".equals(key)) {
            config.enabled(parseBoolean(value, config.enabled()));
        } else if ("permissionLevel".equals(key)) {
            config.permissionLevel(parseInt(value, config.permissionLevel(), 0, 4));
        }
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash >= 0 ? line.substring(0, hash) : line;
    }

    private static String parseString(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        return fallback;
    }

    private static int parseInt(String value, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(value.trim())));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static void command(String name, boolean enabled, int permissionLevel) {
        COMMANDS.put(name, new CommandConfig(enabled, permissionLevel));
    }

    private static void writeDefault(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, defaultConfig(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            PpeEssentials.LOGGER.warn("Failed to write default PPE Essentials config: {}", path, exception);
        }
    }

    private static String defaultConfig() {
        StringBuilder builder = new StringBuilder();
        builder.append("# Fallback language used by the server when the player's client language is unsupported.\n");
        builder.append("# Supported values: en_us, zh_cn, zh_tw, zh_hk, ru_ru, de_de, fr_fr, ja_jp, ko_kr\n");
        builder.append("fallbackLanguage = \"en_us\"\n");
        builder.append("firstJoinNotice = true\n");
        builder.append("# Prevent creepers from breaking blocks with explosions.\n");
        builder.append("preventCreeperBlockDamage = true\n");
        builder.append("# Prevent endermen from picking up or placing blocks.\n");
        builder.append("preventEndermanBlockDamage = true\n");
        builder.append("# Prevent ravagers from breaking leaves and crops.\n");
        builder.append("preventRavagerBlockDamage = true\n");
        builder.append("rtpCooldownSeconds = 30\n");
        builder.append("rtpMinDistance = 2000\n");
        builder.append("rtpMaxDistance = 5000\n");
        builder.append("rtpNetherMinDistance = 600\n");
        builder.append("rtpNetherMaxDistance = 1500\n");
        builder.append("teleportRequestTimeoutSeconds = 60\n");
        builder.append("allowSelfTeleportRequests = true\n\n");
        builder.append("[commands]\n\n");
        for (Map.Entry<String, CommandConfig> entry : COMMANDS.entrySet()) {
            String command = displayCommand(entry.getKey());
            builder.append("[commands.").append(entry.getKey()).append("]\n");
            builder.append("# Whether /").append(command).append(" is registered when the server starts. Changes require a server restart.\n");
            builder.append("enabled = ").append(entry.getValue().enabled()).append("\n");
            builder.append("# Required OP permission level for /").append(command).append(". Use 0 to allow everyone. Changes apply without restart.\n");
            builder.append("permissionLevel = ").append(entry.getValue().permissionLevel()).append("\n\n");
        }
        return builder.toString();
    }

    private static String displayCommand(String command) {
        return "ppe-ess-reset".equals(command) ? "ppe-ess reset" : command;
    }

    private static class CommandConfig {
        private final boolean defaultEnabled;
        private final int defaultPermissionLevel;
        private boolean enabled;
        private int permissionLevel;

        private CommandConfig(boolean enabled, int permissionLevel) {
            this.defaultEnabled = enabled;
            this.defaultPermissionLevel = permissionLevel;
            this.enabled = enabled;
            this.permissionLevel = permissionLevel;
        }

        private boolean enabled() {
            return enabled;
        }

        private void enabled(boolean enabled) {
            this.enabled = enabled;
        }

        private int permissionLevel() {
            return permissionLevel;
        }

        private void permissionLevel(int permissionLevel) {
            this.permissionLevel = permissionLevel;
        }

        private void reset() {
            this.enabled = defaultEnabled;
            this.permissionLevel = defaultPermissionLevel;
        }
    }
}

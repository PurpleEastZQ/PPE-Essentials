package me.purpleeast.mods.ppe_essential;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PpeLang {
    public static final String PREFIX = "§7[§dPPE§7] ";

    private static final Map<String, Map<String, String>> LANGUAGES = Map.of(
            "zh_cn", load("zh_cn"),
            "zh_tw", load("zh_tw"),
            "zh_hk", load("zh_tw"),
            "en_us", load("en_us"),
            "ru_ru", load("ru_ru"),
            "de_de", load("de_de"),
            "fr_fr", load("fr_fr"),
            "ja_jp", load("ja_jp"),
            "ko_kr", load("ko_kr")
    );

    public static MutableComponent component(String key, Object... args) {
        return componentForLanguage(PpeConfig.fallbackLanguage(), key, args);
    }

    public static MutableComponent component(ServerPlayer player, String key, Object... args) {
        return componentForLanguage(languageFor(player), key, args);
    }

    public static MutableComponent componentForLanguage(String language, String key, Object... args) {
        return Component.literal(format(language, key, args));
    }

    public static MutableComponent prefixedComponent(ServerPlayer player, String key, Object... args) {
        return prefixed(component(player, key, args));
    }

    public static MutableComponent prefixed(Component component) {
        return Component.literal(PREFIX).append(component);
    }

    public static String format(String key, Object... args) {
        return format(PpeConfig.fallbackLanguage(), key, args);
    }

    public static String languageFor(ServerPlayer player) {
        String clientLanguage = player.clientInformation().language();
        if (LANGUAGES.containsKey(clientLanguage)) {
            return clientLanguage;
        }

        String configuredLanguage = PpeConfig.fallbackLanguage();
        if (LANGUAGES.containsKey(configuredLanguage)) {
            return configuredLanguage;
        }

        return "en_us";
    }

    private static String format(String language, String key, Object... args) {
        String template = LANGUAGES.getOrDefault(language, LANGUAGES.get("en_us")).get(key);
        if (template == null) {
            template = LANGUAGES.get("en_us").getOrDefault(key, key);
        }

        Object[] plainArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            plainArgs[i] = toPlainText(args[i]);
        }

        try {
            return String.format(Locale.ROOT, template, plainArgs);
        } catch (RuntimeException ignored) {
            return template;
        }
    }

    private static Object toPlainText(Object arg) {
        return arg instanceof Component component ? component.getString() : arg;
    }

    private static Map<String, String> load(String language) {
        String path = "/assets/" + PpeEssential.MODID + "/lang/" + language + ".json";
        try (InputStream stream = PpeLang.class.getResourceAsStream(path)) {
            if (stream == null) {
                PpeEssential.LOGGER.warn("Missing PPE Essential language file: {}", path);
                return Map.of();
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, String> values = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                values.put(entry.getKey(), entry.getValue().getAsString());
            }
            return values;
        } catch (Exception exception) {
            PpeEssential.LOGGER.warn("Failed to load PPE Essential language file: {}", path, exception);
            return Map.of();
        }
    }
}

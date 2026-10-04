package com.velocity.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocity.client.module.Module;
import com.velocity.client.module.ModuleManager;
import com.velocity.client.module.hud.HudModule;
import com.velocity.client.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Saves Velocity's settings and module layout to config/velocity-client.json. */
public final class VelocityConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "velocity-client.json";

    public static final String[] ACCENT_NAMES = {"Violet", "Ocean", "Aqua", "Mint", "Sunset", "Rose", "Gold"};
    public static final int[] ACCENTS = {0xFF8C7CFF, 0xFF4C8DFF, 0xFF27D3E6, 0xFF2BD47D, 0xFFFF8A3D, 0xFFFF5C9A, 0xFFF5C542};

    public static int accent = 0;
    public static boolean customMainMenu = true;
    public static boolean animations = true;
    public static double chromaSpeed = 1.0;
    public static boolean firstRunDone = false;
    public static String preset = "Balanced";
    /** Restyle vanilla buttons, sliders and menu backgrounds with the Velocity look. */
    public static boolean uiTheme = true;
    /** Dark recolour of inventory / container / advancement windows. */
    public static boolean darkInventories = true;
    /** Fade vanilla screens in when they open. */
    public static boolean screenAnimations = true;
    /** Toasts like "Zoom enabled". */
    public static boolean notifications = true;

    private VelocityConfig() {}

    public static int accentColor() {
        return ACCENTS[Math.floorMod(accent, ACCENTS.length)];
    }

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static void load() {
        Path path = path();
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            accent = getInt(root, "accent", accent);
            customMainMenu = getBool(root, "customMainMenu", customMainMenu);
            animations = getBool(root, "animations", animations);
            chromaSpeed = root.has("chromaSpeed") ? root.get("chromaSpeed").getAsDouble() : chromaSpeed;
            firstRunDone = getBool(root, "firstRunDone", firstRunDone);
            preset = root.has("preset") ? root.get("preset").getAsString() : preset;
            uiTheme = getBool(root, "uiTheme", uiTheme);
            darkInventories = getBool(root, "darkInventories", darkInventories);
            screenAnimations = getBool(root, "screenAnimations", screenAnimations);
            notifications = getBool(root, "notifications", notifications);

            JsonObject modules = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module module : ModuleManager.all()) {
                if (!modules.has(module.getId())) continue;
                JsonObject m = modules.getAsJsonObject(module.getId());
                module.setEnabledSilently(getBool(m, "enabled", module.isEnabled()));
                if (module instanceof HudModule hud) {
                    if (m.has("x")) hud.x = m.get("x").getAsFloat();
                    if (m.has("y")) hud.y = m.get("y").getAsFloat();
                }
                JsonObject settings = m.has("settings") ? m.getAsJsonObject("settings") : new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    JsonElement value = settings.get(setting.getName());
                    if (value != null) setting.fromJson(value);
                }
            }
        } catch (Exception e) {
            VelocityClient.LOGGER.warn("Could not read Velocity config, using defaults", e);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("accent", accent);
        root.addProperty("customMainMenu", customMainMenu);
        root.addProperty("animations", animations);
        root.addProperty("chromaSpeed", chromaSpeed);
        root.addProperty("firstRunDone", firstRunDone);
        root.addProperty("preset", preset);
        root.addProperty("uiTheme", uiTheme);
        root.addProperty("darkInventories", darkInventories);
        root.addProperty("screenAnimations", screenAnimations);
        root.addProperty("notifications", notifications);

        JsonObject modules = new JsonObject();
        for (Module module : ModuleManager.all()) {
            JsonObject m = new JsonObject();
            m.addProperty("enabled", module.isEnabled());
            if (module instanceof HudModule hud) {
                m.addProperty("x", hud.x);
                m.addProperty("y", hud.y);
            }
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.getSettings()) settings.add(setting.getName(), setting.toJson());
            m.add("settings", settings);
            modules.add(module.getId(), m);
        }
        root.add("modules", modules);

        try {
            Files.createDirectories(path().getParent());
            try (Writer writer = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            VelocityClient.LOGGER.warn("Could not save Velocity config", e);
        }
    }

    private static boolean getBool(JsonObject o, String key, boolean fallback) {
        return o.has(key) ? o.get(key).getAsBoolean() : fallback;
    }

    private static int getInt(JsonObject o, String key, int fallback) {
        return o.has(key) ? o.get(key).getAsInt() : fallback;
    }
}

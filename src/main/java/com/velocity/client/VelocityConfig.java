package com.velocity.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Small dependency-free config file for Velocity's client preferences. */
public final class VelocityConfig {
    private static final String FILE_NAME = "velocity-client.properties";
    private static final Properties PROPERTIES = new Properties();

    public static boolean performanceProfile = true;
    public static boolean fpsHud = true;
    public static boolean coordsHud = false;
    public static boolean sprintHud = true;
    public static boolean zoom = true;

    private VelocityConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static void load() {
        Path path = path();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                PROPERTIES.load(in);
                performanceProfile = bool("performanceProfile", performanceProfile);
                fpsHud = bool("fpsHud", fpsHud);
                coordsHud = bool("coordsHud", coordsHud);
                sprintHud = bool("sprintHud", sprintHud);
                zoom = bool("zoom", zoom);
            } catch (IOException ignored) {
                // Keep defaults when the config cannot be read.
            }
        }
    }

    public static void save() {
        PROPERTIES.setProperty("performanceProfile", Boolean.toString(performanceProfile));
        PROPERTIES.setProperty("fpsHud", Boolean.toString(fpsHud));
        PROPERTIES.setProperty("coordsHud", Boolean.toString(coordsHud));
        PROPERTIES.setProperty("sprintHud", Boolean.toString(sprintHud));
        PROPERTIES.setProperty("zoom", Boolean.toString(zoom));

        try {
            Files.createDirectories(path().getParent());
            try (OutputStream out = Files.newOutputStream(path())) {
                PROPERTIES.store(out, "Velocity Client settings");
            }
        } catch (IOException ignored) {
            // Configuration is best-effort; the game should keep running.
        }
    }

    private static boolean bool(String key, boolean fallback) {
        String value = PROPERTIES.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }
}

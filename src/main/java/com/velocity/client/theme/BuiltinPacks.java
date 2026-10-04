package com.velocity.client.theme;

import com.velocity.client.VelocityClient;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Resource packs that ship inside the Velocity jar (drawn by tools/generate_packs.py). */
public final class BuiltinPacks {
    public static final String ICONS = "velocity_icons";
    public static final String CLEAR_GLASS = "velocity_clear_glass";

    private BuiltinPacks() {}

    public static void register() {
        FabricLoader.getInstance().getModContainer(VelocityClient.MOD_ID).ifPresent(mod -> {
            ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(VelocityClient.MOD_ID, ICONS), mod,
                    Text.literal("Velocity Icons"), ResourcePackActivationType.DEFAULT_ENABLED);
            ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(VelocityClient.MOD_ID, CLEAR_GLASS), mod,
                    Text.literal("Velocity Clear Glass"), ResourcePackActivationType.DEFAULT_ENABLED);
        });
    }

    /** Finds the pack's id in the pack manager (Fabric prefixes it), or null if missing. */
    private static String find(ResourcePackManager manager, String name) {
        for (String id : manager.getIds()) {
            if (id.endsWith(name)) return id;
        }
        return null;
    }

    public static boolean isEnabled(String name) {
        ResourcePackManager manager = MinecraftClient.getInstance().getResourcePackManager();
        String id = find(manager, name);
        return id != null && manager.getEnabledIds().contains(id);
    }

    public static boolean exists(String name) {
        return find(MinecraftClient.getInstance().getResourcePackManager(), name) != null;
    }

    /** Turns a built-in pack on or off and reloads resources (takes a few seconds, like the Resource Packs screen). */
    public static void toggle(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        ResourcePackManager manager = client.getResourcePackManager();
        String id = find(manager, name);
        if (id == null) return;
        if (manager.getEnabledIds().contains(id)) manager.disable(id);
        else manager.enable(id);
        client.options.refreshResourcePacks(manager);
    }
}

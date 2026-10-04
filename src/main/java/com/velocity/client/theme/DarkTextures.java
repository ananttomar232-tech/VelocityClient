package com.velocity.client.theme;

import com.velocity.client.VelocityClient;
import com.velocity.client.VelocityConfig;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Recolours the grey vanilla container / advancement windows into Velocity's dark theme at runtime.
 * It works with any resource pack because it reads whatever texture is currently loaded,
 * and only grey pixels are touched, so icons and arrows keep their colours.
 */
public final class DarkTextures implements SimpleSynchronousResourceReloadListener {
    private static final List<String> PATHS = List.of(
            "textures/gui/container/inventory.png",
            "textures/gui/container/generic_54.png",
            "textures/gui/container/crafting_table.png",
            "textures/gui/container/furnace.png",
            "textures/gui/container/blast_furnace.png",
            "textures/gui/container/smoker.png",
            "textures/gui/container/shulker_box.png",
            "textures/gui/container/dispenser.png",
            "textures/gui/container/hopper.png",
            "textures/gui/container/anvil.png",
            "textures/gui/container/enchanting_table.png",
            "textures/gui/container/brewing_stand.png",
            "textures/gui/container/beacon.png",
            "textures/gui/container/grindstone.png",
            "textures/gui/container/smithing.png",
            "textures/gui/container/stonecutter.png",
            "textures/gui/container/loom.png",
            "textures/gui/container/cartography_table.png",
            "textures/gui/container/horse.png",
            "textures/gui/container/crafter.png",
            "textures/gui/container/creative_inventory/tab_items.png",
            "textures/gui/container/creative_inventory/tab_inventory.png",
            "textures/gui/container/creative_inventory/tab_item_search.png",
            "textures/gui/advancements/window.png",
            "textures/gui/recipe_book.png");

    // Grey level in -> dark level out (piecewise linear), tuned to vanilla's GUI palette.
    private static final int[] IN = {0x00, 0x37, 0x55, 0x8B, 0xC6, 0xFF};
    private static final int[] OUT = {0x04, 0x08, 0x0E, 0x13, 0x1D, 0x3B};

    @Override
    public Identifier getFabricId() {
        return Identifier.of(VelocityClient.MOD_ID, "dark_textures");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(ResourceReloadListenerKeys.TEXTURES);
    }

    @Override
    public void reload(ResourceManager manager) {
        MinecraftClient.getInstance().execute(DarkTextures::apply);
    }

    /** Applies (or removes) the dark textures according to the current setting. */
    public static void apply() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getResourceManager() == null) return;
        TextureManager textures = client.getTextureManager();
        for (String path : PATHS) {
            Identifier id = Identifier.ofVanilla(path);
            if (!VelocityConfig.darkInventories) {
                // Forget our copy; vanilla reloads the original from resources next time it is drawn.
                textures.destroyTexture(id);
                continue;
            }
            Optional<Resource> resource = client.getResourceManager().getResource(id);
            if (resource.isEmpty()) continue;
            try (InputStream in = resource.get().getInputStream()) {
                NativeImage image = NativeImage.read(in);
                recolor(image);
                textures.registerTexture(id, new NativeImageBackedTexture(image));
            } catch (Exception e) {
                VelocityClient.LOGGER.warn("Could not theme {}", path, e);
            }
        }
    }

    private static void recolor(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getColor(x, y);
                int a = abgr >>> 24;
                if (a == 0) continue;
                int r = abgr & 255, g = abgr >> 8 & 255, b = abgr >> 16 & 255;
                if (Math.abs(r - g) > 10 || Math.abs(g - b) > 10) continue; // coloured pixel: keep
                int v = map((r + g + b) / 3);
                int nr = v, ng = Math.min(255, v + 3), nb = Math.min(255, v + 10); // slight blue tint
                int na = Math.round(a * (float) VelocityConfig.containerOpacity);
                image.setColor(x, y, na << 24 | nb << 16 | ng << 8 | nr);
            }
        }
    }

    private static int map(int grey) {
        for (int i = 1; i < IN.length; i++) {
            if (grey <= IN[i]) {
                float t = (grey - IN[i - 1]) / (float) (IN[i] - IN[i - 1]);
                return Math.round(OUT[i - 1] + (OUT[i] - OUT[i - 1]) * t);
            }
        }
        return OUT[OUT.length - 1];
    }
}

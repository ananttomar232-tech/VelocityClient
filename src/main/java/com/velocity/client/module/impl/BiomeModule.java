package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.Locale;

public final class BiomeModule extends TextHudModule {
    public BiomeModule() {
        super("biome", "Biome", "The biome you are standing in.", Category.HUD, Items.OAK_SAPLING, false, 0.4f, 0.07f);
    }

    @Override
    protected String label() {
        return "Biome";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        if (client.world == null || client.player == null) return "Cherry Grove";
        String path = client.world.getBiome(client.player.getBlockPos()).getKey()
                .map(key -> key.getValue().getPath()).orElse("unknown");
        StringBuilder out = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return out.toString();
    }
}

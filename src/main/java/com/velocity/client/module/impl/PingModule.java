package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Items;

public final class PingModule extends TextHudModule {
    public PingModule() {
        super("ping", "Ping", "Your connection latency to the server.", Category.HUD, Items.ENDER_PEARL, false, 0.005f, 0.25f);
    }

    @Override
    protected String label() {
        return "Ping";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        if (client.player == null || client.getNetworkHandler() == null) return "24 ms";
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return (entry == null ? 0 : entry.getLatency()) + " ms";
    }
}

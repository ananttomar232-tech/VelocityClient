package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class ServerAddressModule extends TextHudModule {
    public ServerAddressModule() {
        super("server", "Server Address", "Shows which server you are playing on.", Category.HUD, Items.NAME_TAG, false, 0.4f, 0.01f);
    }

    @Override
    public boolean hasContent(MinecraftClient client) {
        return client.getCurrentServerEntry() != null;
    }

    @Override
    protected String label() {
        return "IP";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        var server = client.getCurrentServerEntry();
        return server == null ? "play.example.net" : server.address;
    }
}

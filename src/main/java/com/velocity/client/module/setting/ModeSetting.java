package com.velocity.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public boolean is(String mode) {
        return value.equals(mode);
    }

    public void cycle(int direction) {
        int index = modes.indexOf(value);
        index = Math.floorMod(index + direction, modes.size());
        value = modes.get(index);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive() && modes.contains(json.getAsString())) value = json.getAsString();
    }
}

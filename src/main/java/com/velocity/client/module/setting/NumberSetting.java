package com.velocity.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    /** 0..1 position of the current value between min and max. */
    public double progress() {
        return (value - min) / (max - min);
    }

    public void setFromProgress(double progress) {
        set(min + Math.max(0, Math.min(1, progress)) * (max - min));
    }

    @Override
    public void set(Double newValue) {
        double snapped = Math.round(newValue / step) * step;
        super.set(Math.max(min, Math.min(max, snapped)));
    }

    public float getFloat() {
        return value.floatValue();
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public String display() {
        return step >= 1 ? Integer.toString(getInt()) : String.format("%.1f", value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) set(json.getAsDouble());
    }
}

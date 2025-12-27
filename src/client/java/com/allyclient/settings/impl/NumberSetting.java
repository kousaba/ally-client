package com.allyclient.settings.impl;

import com.allyclient.settings.Setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting {
    private double value;
    private final double min;
    private final double max;

    public NumberSetting(String name, double defaultValue, double min, double max) {
        super(name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        // 最小値～最大値の範囲に収める (clamp)
        this.value = Math.max(min, Math.min(max, value));
    }

    public double getMin() { return min; }
    public double getMax() { return max; }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            setValue(element.getAsDouble());
        }
    }
}
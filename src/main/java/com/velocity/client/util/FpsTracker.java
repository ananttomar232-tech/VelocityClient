package com.velocity.client.util;

import net.minecraft.client.MinecraftClient;

/** Keeps the last minute of FPS samples for the graph on the Performance page. */
public final class FpsTracker {
    public static final int SIZE = 60;
    private static final int[] SAMPLES = new int[SIZE];
    private static int head;
    private static int count;
    private static int ticks;

    private FpsTracker() {}

    public static void tick(MinecraftClient client) {
        if (++ticks < 20) return;
        ticks = 0;
        SAMPLES[head] = client.getCurrentFps();
        head = (head + 1) % SIZE;
        count = Math.min(SIZE, count + 1);
    }

    public static int count() {
        return count;
    }

    /** i = 0 is the oldest sample. */
    public static int get(int i) {
        return SAMPLES[Math.floorMod(head - count + i, SIZE)];
    }

    public static int average() {
        if (count == 0) return 0;
        long sum = 0;
        for (int i = 0; i < count; i++) sum += get(i);
        return (int) (sum / count);
    }

    public static int max() {
        int max = 0;
        for (int i = 0; i < count; i++) max = Math.max(max, get(i));
        return max;
    }
}

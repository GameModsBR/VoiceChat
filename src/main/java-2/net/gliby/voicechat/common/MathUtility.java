/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.common;

public class MathUtility {
    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}


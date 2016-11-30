/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class Misc {
    public static float[] window(int n, int n2) {
        int n3;
        int n4 = n2 * 7 / 2;
        int n5 = n2 * 5 / 2;
        float[] arrf = new float[n];
        for (n3 = 0; n3 < n4; ++n3) {
            arrf[n3] = (float)(0.54 - 0.46 * Math.cos(3.141592653589793 * (double)n3 / (double)n4));
        }
        for (n3 = 0; n3 < n5; ++n3) {
            arrf[n4 + n3] = (float)(0.54 + 0.46 * Math.cos(3.141592653589793 * (double)n3 / (double)n5));
        }
        return arrf;
    }

    public static float[] lagWindow(int n, float f) {
        float[] arrf = new float[n + 1];
        for (int i = 0; i < n + 1; ++i) {
            arrf[i] = (float)Math.exp(-0.5 * (6.283185307179586 * (double)f * (double)i) * (6.283185307179586 * (double)f * (double)i));
        }
        return arrf;
    }
}


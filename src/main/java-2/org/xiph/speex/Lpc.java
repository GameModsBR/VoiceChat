/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class Lpc {
    public static float wld(float[] arrf, float[] arrf2, float[] arrf3, int n) {
        float f = arrf2[0];
        if (arrf2[0] == 0.0f) {
            for (int i = 0; i < n; ++i) {
                arrf3[i] = 0.0f;
            }
            return 0.0f;
        }
        for (int i = 0; i < n; ++i) {
            int n2;
            float f2 = - arrf2[i + 1];
            for (n2 = 0; n2 < i; ++n2) {
                f2 -= arrf[n2] * arrf2[i - n2];
            }
            arrf3[i] = f2 /= f;
            arrf[i] = f2;
            for (n2 = 0; n2 < i / 2; ++n2) {
                float f3 = arrf[n2];
                float[] arrf4 = arrf;
                int n3 = n2;
                arrf4[n3] = arrf4[n3] + f2 * arrf[i - 1 - n2];
                float[] arrf5 = arrf;
                int n4 = i - 1 - n2;
                arrf5[n4] = arrf5[n4] + f2 * f3;
            }
            if (i % 2 != 0) {
                float[] arrf6 = arrf;
                int n5 = n2;
                arrf6[n5] = arrf6[n5] + arrf[n2] * f2;
            }
            f = (float)((double)f * (1.0 - (double)(f2 * f2)));
        }
        return f;
    }

    public static void autocorr(float[] arrf, float[] arrf2, int n, int n2) {
        while (n-- > 0) {
            float f = 0.0f;
            for (int i = n; i < n2; ++i) {
                f += arrf[i] * arrf[i - n];
            }
            arrf2[n] = f;
        }
    }
}


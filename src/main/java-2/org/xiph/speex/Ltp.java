/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;

public abstract class Ltp {
    public abstract int quant(float[] var1, float[] var2, int var3, float[] var4, float[] var5, float[] var6, float[] var7, int var8, int var9, int var10, float var11, int var12, int var13, Bits var14, float[] var15, int var16, float[] var17, int var18);

    public abstract int unquant(float[] var1, int var2, int var3, float var4, int var5, float[] var6, Bits var7, int var8, int var9, float var10);

    protected static float inner_prod(float[] arrf, int n, float[] arrf2, int n2, int n3) {
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        for (int i = 0; i < n3; i += 4) {
            f += arrf[n + i] * arrf2[n2 + i];
            f2 += arrf[n + i + 1] * arrf2[n2 + i + 1];
            f3 += arrf[n + i + 2] * arrf2[n2 + i + 2];
            f4 += arrf[n + i + 3] * arrf2[n2 + i + 3];
        }
        return f + f2 + f3 + f4;
    }

    protected static void open_loop_nbest_pitch(float[] arrf, int n, int n2, int n3, int n4, int[] arrn, float[] arrf2, int n5) {
        int n6;
        float[] arrf3 = new float[n5];
        float[] arrf4 = new float[n3 - n2 + 1];
        float[] arrf5 = new float[n3 - n2 + 2];
        float[] arrf6 = new float[n3 - n2 + 1];
        for (n6 = 0; n6 < n5; ++n6) {
            arrf3[n6] = -1.0f;
            arrf2[n6] = 0.0f;
            arrn[n6] = n2;
        }
        arrf5[0] = Ltp.inner_prod(arrf, n - n2, arrf, n - n2, n4);
        float f = Ltp.inner_prod(arrf, n, arrf, n, n4);
        for (n6 = n2; n6 <= n3; ++n6) {
            arrf5[n6 - n2 + 1] = arrf5[n6 - n2] + arrf[n - n6 - 1] * arrf[n - n6 - 1] - arrf[n - n6 + n4 - 1] * arrf[n - n6 + n4 - 1];
            if (arrf5[n6 - n2 + 1] >= 1.0f) continue;
            arrf5[n6 - n2 + 1] = 1.0f;
        }
        for (n6 = n2; n6 <= n3; ++n6) {
            arrf4[n6 - n2] = 0.0f;
            arrf6[n6 - n2] = 0.0f;
        }
        for (n6 = n2; n6 <= n3; ++n6) {
            arrf4[n6 - n2] = Ltp.inner_prod(arrf, n, arrf, n - n6, n4);
            arrf6[n6 - n2] = arrf4[n6 - n2] * arrf4[n6 - n2] / (arrf5[n6 - n2] + 1.0f);
        }
        block4 : for (n6 = n2; n6 <= n3; ++n6) {
            if (arrf6[n6 - n2] <= arrf3[n5 - 1]) continue;
            float f2 = arrf4[n6 - n2] / (arrf5[n6 - n2] + 10.0f);
            float f3 = (float)Math.sqrt(f2 * arrf4[n6 - n2] / (f + 10.0f));
            if (f3 > f2) {
                f3 = f2;
            }
            if (f3 < 0.0f) {
                f3 = 0.0f;
            }
            for (int i = 0; i < n5; ++i) {
                if (arrf6[n6 - n2] <= arrf3[i]) continue;
                for (int j = n5 - 1; j > i; --j) {
                    arrf3[j] = arrf3[j - 1];
                    arrn[j] = arrn[j - 1];
                    arrf2[j] = arrf2[j - 1];
                }
                arrf3[i] = arrf6[n6 - n2];
                arrn[i] = n6;
                arrf2[i] = f3;
                continue block4;
            }
        }
    }
}


/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.Codebook;

public abstract class LspQuant
implements Codebook {
    public static final int MAX_LSP_SIZE = 20;

    protected LspQuant() {
    }

    public abstract void quant(float[] var1, float[] var2, int var3, Bits var4);

    public abstract void unquant(float[] var1, int var2, Bits var3);

    protected void unpackPlus(float[] arrf, int[] arrn, Bits bits, float f, int n, int n2) {
        int n3 = bits.unpack(6);
        for (int i = 0; i < n; ++i) {
            float[] arrf2 = arrf;
            int n4 = i + n2;
            arrf2[n4] = arrf2[n4] + f * (float)arrn[n3 * n + i];
        }
    }

    protected static int lsp_quant(float[] arrf, int n, int[] arrn, int n2, int n3) {
        int n4;
        float f = 0.0f;
        int n5 = 0;
        int n6 = 0;
        for (int i = 0; i < n2; ++i) {
            float f2 = 0.0f;
            for (n4 = 0; n4 < n3; ++n4) {
                float f3 = arrf[n + n4] - (float)arrn[n6++];
                f2 += f3 * f3;
            }
            if (f2 >= f && i != 0) continue;
            f = f2;
            n5 = i;
        }
        for (n4 = 0; n4 < n3; ++n4) {
            float[] arrf2 = arrf;
            int n7 = n + n4;
            arrf2[n7] = arrf2[n7] - (float)arrn[n5 * n3 + n4];
        }
        return n5;
    }

    protected static int lsp_weight_quant(float[] arrf, int n, float[] arrf2, int n2, int[] arrn, int n3, int n4) {
        int n5;
        float f = 0.0f;
        int n6 = 0;
        int n7 = 0;
        for (int i = 0; i < n3; ++i) {
            float f2 = 0.0f;
            for (n5 = 0; n5 < n4; ++n5) {
                float f3 = arrf[n + n5] - (float)arrn[n7++];
                f2 += arrf2[n2 + n5] * f3 * f3;
            }
            if (f2 >= f && i != 0) continue;
            f = f2;
            n6 = i;
        }
        for (n5 = 0; n5 < n4; ++n5) {
            float[] arrf3 = arrf;
            int n8 = n + n5;
            arrf3[n8] = arrf3[n8] - (float)arrn[n6 * n4 + n5];
        }
        return n6;
    }
}


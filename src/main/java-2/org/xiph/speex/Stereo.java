/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.VQ;

public class Stereo {
    public static final int SPEEX_INBAND_STEREO = 9;
    public static final float[] e_ratio_quant = new float[]{0.25f, 0.315f, 0.397f, 0.5f};
    private float balance = 1.0f;
    private float e_ratio = 0.5f;
    private float smooth_left = 1.0f;
    private float smooth_right = 1.0f;

    public static void encode(Bits bits, float[] arrf, int n) {
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        for (int i = 0; i < n; ++i) {
            f += arrf[2 * i] * arrf[2 * i];
            f2 += arrf[2 * i + 1] * arrf[2 * i + 1];
            arrf[i] = 0.5f * (arrf[2 * i] + arrf[2 * i + 1]);
            f3 += arrf[i] * arrf[i];
        }
        float f4 = (f + 1.0f) / (f2 + 1.0f);
        float f5 = f3 / (1.0f + f + f2);
        bits.pack(14, 5);
        bits.pack(9, 4);
        f4 = (float)(4.0 * Math.log(f4));
        if (f4 > 0.0f) {
            bits.pack(0, 1);
        } else {
            bits.pack(1, 1);
        }
        f4 = (float)Math.floor(0.5f + Math.abs(f4));
        if (f4 > 30.0f) {
            f4 = 31.0f;
        }
        bits.pack((int)f4, 5);
        int n2 = VQ.index(f5, e_ratio_quant, 4);
        bits.pack(n2, 2);
    }

    public void decode(float[] arrf, int n) {
        int n2;
        float f = 0.0f;
        for (n2 = n - 1; n2 >= 0; --n2) {
            f += arrf[n2] * arrf[n2];
        }
        float f2 = f / this.e_ratio;
        float f3 = f2 * this.balance / (1.0f + this.balance);
        float f4 = f2 - f3;
        f3 = (float)Math.sqrt(f3 / (f + 0.01f));
        f4 = (float)Math.sqrt(f4 / (f + 0.01f));
        for (n2 = n - 1; n2 >= 0; --n2) {
            float f5 = arrf[n2];
            this.smooth_left = 0.98f * this.smooth_left + 0.02f * f3;
            this.smooth_right = 0.98f * this.smooth_right + 0.02f * f4;
            arrf[2 * n2] = this.smooth_left * f5;
            arrf[2 * n2 + 1] = this.smooth_right * f5;
        }
    }

    public void init(Bits bits) {
        float f = 1.0f;
        if (bits.unpack(1) != 0) {
            f = -1.0f;
        }
        int n = bits.unpack(5);
        this.balance = (float)Math.exp((double)f * 0.25 * (double)n);
        n = bits.unpack(2);
        this.e_ratio = e_ratio_quant[n];
    }
}


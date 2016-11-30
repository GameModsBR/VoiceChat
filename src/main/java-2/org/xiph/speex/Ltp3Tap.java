/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.Filters;
import org.xiph.speex.Ltp;

public class Ltp3Tap
extends Ltp {
    private float[] gain = new float[3];
    private int[] gain_cdbk;
    private int gain_bits;
    private int pitch_bits;
    private float[][] e;

    public Ltp3Tap(int[] arrn, int n, int n2) {
        this.gain_cdbk = arrn;
        this.gain_bits = n;
        this.pitch_bits = n2;
        this.e = new float[3][128];
    }

    public final int quant(float[] arrf, float[] arrf2, int n, float[] arrf3, float[] arrf4, float[] arrf5, float[] arrf6, int n2, int n3, int n4, float f, int n5, int n6, Bits bits, float[] arrf7, int n7, float[] arrf8, int n8) {
        int n9;
        int[] arrn = new int[1];
        int n10 = 0;
        int n11 = 0;
        int n12 = 0;
        float f2 = -1.0f;
        int n13 = n8;
        if (n13 > 10) {
            n13 = 10;
        }
        int[] arrn2 = new int[n13];
        float[] arrf9 = new float[n13];
        if (n13 == 0 || n4 < n3) {
            bits.pack(0, this.pitch_bits);
            bits.pack(0, this.gain_bits);
            for (int i = 0; i < n6; ++i) {
                arrf6[n2 + i] = 0.0f;
            }
            return n3;
        }
        float[] arrf10 = new float[n6];
        if (n13 > n4 - n3 + 1) {
            n13 = n4 - n3 + 1;
        }
        Ltp3Tap.open_loop_nbest_pitch(arrf2, n, n3, n4, n6, arrn2, arrf9, n13);
        for (n9 = 0; n9 < n13; ++n9) {
            int n14;
            n10 = arrn2[n9];
            for (n14 = 0; n14 < n6; ++n14) {
                arrf6[n2 + n14] = 0.0f;
            }
            float f3 = this.pitch_gain_search_3tap(arrf, arrf3, arrf4, arrf5, arrf6, n2, n10, n5, n6, bits, arrf7, n7, arrf8, arrn);
            if (f3 >= f2 && f2 >= 0.0f) continue;
            for (n14 = 0; n14 < n6; ++n14) {
                arrf10[n14] = arrf6[n2 + n14];
            }
            f2 = f3;
            n12 = n10;
            n11 = arrn[0];
        }
        bits.pack(n12 - n3, this.pitch_bits);
        bits.pack(n11, this.gain_bits);
        for (n9 = 0; n9 < n6; ++n9) {
            arrf6[n2 + n9] = arrf10[n9];
        }
        return n10;
    }

    public final int unquant(float[] arrf, int n, int n2, float f, int n3, float[] arrf2, Bits bits, int n4, int n5, float f2) {
        int n6;
        int n7 = bits.unpack(this.pitch_bits);
        int n8 = bits.unpack(this.gain_bits);
        this.gain[0] = 0.015625f * (float)this.gain_cdbk[n8 * 3] + 0.5f;
        this.gain[1] = 0.015625f * (float)this.gain_cdbk[n8 * 3 + 1] + 0.5f;
        this.gain[2] = 0.015625f * (float)this.gain_cdbk[n8 * 3 + 2] + 0.5f;
        if (n4 != 0 && (n7 += n2) > n5) {
            float f3;
            float f4 = Math.abs(this.gain[1]);
            float f5 = f3 = n4 < 4 ? f2 : 0.4f * f2;
            if (f3 > 0.95f) {
                f3 = 0.95f;
            }
            f4 = this.gain[0] > 0.0f ? (f4 += this.gain[0]) : (f4 -= 0.5f * this.gain[0]);
            f4 = this.gain[2] > 0.0f ? (f4 += this.gain[2]) : (f4 -= 0.5f * this.gain[0]);
            if (f4 > f3) {
                float f6 = f3 / f4;
                n6 = 0;
                while (n6 < 3) {
                    float[] arrf3 = this.gain;
                    int n9 = n6++;
                    arrf3[n9] = arrf3[n9] * f6;
                }
            }
        }
        arrf2[0] = this.gain[0];
        arrf2[1] = this.gain[1];
        arrf2[2] = this.gain[2];
        for (n6 = 0; n6 < 3; ++n6) {
            int n10;
            int n11;
            int n12 = n3;
            int n13 = n7 + 1 - n6;
            if (n12 > n13) {
                n12 = n13;
            }
            if ((n11 = n3) > n13 + n7) {
                n11 = n13 + n7;
            }
            for (n10 = 0; n10 < n12; ++n10) {
                this.e[n6][n10] = arrf[n + n10 - n13];
            }
            for (n10 = n12; n10 < n11; ++n10) {
                this.e[n6][n10] = arrf[n + n10 - n13 - n7];
            }
            for (n10 = n11; n10 < n3; ++n10) {
                this.e[n6][n10] = 0.0f;
            }
        }
        for (n6 = 0; n6 < n3; ++n6) {
            arrf[n + n6] = this.gain[0] * this.e[2][n6] + this.gain[1] * this.e[1][n6] + this.gain[2] * this.e[0][n6];
        }
        return n7;
    }

    private float pitch_gain_search_3tap(float[] arrf, float[] arrf2, float[] arrf3, float[] arrf4, float[] arrf5, int n, int n2, int n3, int n4, Bits bits, float[] arrf6, int n5, float[] arrf7, int[] arrn) {
        int n6;
        int n7;
        float[] arrf8 = new float[3];
        float[][] arrf9 = new float[3][3];
        int n8 = 1 << this.gain_bits;
        float[][] arrf10 = new float[3][n4];
        this.e = new float[3][n4];
        for (n7 = 2; n7 >= 0; --n7) {
            int n9 = n2 + 1 - n7;
            for (n6 = 0; n6 < n4; ++n6) {
                this.e[n7][n6] = n6 - n9 < 0 ? arrf6[n5 + n6 - n9] : (n6 - n9 - n2 < 0 ? arrf6[n5 + n6 - n9 - n2] : 0.0f);
            }
            if (n7 == 2) {
                Filters.syn_percep_zero(this.e[n7], 0, arrf2, arrf3, arrf4, arrf10[n7], n4, n3);
                continue;
            }
            for (n6 = 0; n6 < n4 - 1; ++n6) {
                arrf10[n7][n6 + 1] = arrf10[n7 + 1][n6];
            }
            arrf10[n7][0] = 0.0f;
            for (n6 = 0; n6 < n4; ++n6) {
                float[] arrf11 = arrf10[n7];
                int n10 = n6;
                arrf11[n10] = arrf11[n10] + this.e[n7][0] * arrf7[n6];
            }
        }
        for (n7 = 0; n7 < 3; ++n7) {
            arrf8[n7] = Ltp3Tap.inner_prod(arrf10[n7], 0, arrf, 0, n4);
        }
        for (n7 = 0; n7 < 3; ++n7) {
            for (n6 = 0; n6 <= n7; ++n6) {
                float f = Ltp3Tap.inner_prod(arrf10[n7], 0, arrf10[n6], 0, n4);
                arrf9[n6][n7] = f;
                arrf9[n7][n6] = f;
            }
        }
        float[] arrf12 = new float[9];
        int n11 = 0;
        int n12 = 0;
        float f = 0.0f;
        arrf12[0] = arrf8[2];
        arrf12[1] = arrf8[1];
        arrf12[2] = arrf8[0];
        arrf12[3] = arrf9[1][2];
        arrf12[4] = arrf9[0][1];
        arrf12[5] = arrf9[0][2];
        arrf12[6] = arrf9[2][2];
        arrf12[7] = arrf9[1][1];
        arrf12[8] = arrf9[0][0];
        for (n7 = 0; n7 < n8; ++n7) {
            float f2 = 0.0f;
            n11 = 3 * n7;
            float f3 = 0.015625f * (float)this.gain_cdbk[n11] + 0.5f;
            float f4 = 0.015625f * (float)this.gain_cdbk[n11 + 1] + 0.5f;
            float f5 = 0.015625f * (float)this.gain_cdbk[n11 + 2] + 0.5f;
            f2 += arrf12[0] * f3;
            f2 += arrf12[1] * f4;
            f2 += arrf12[2] * f5;
            f2 -= arrf12[3] * f3 * f4;
            f2 -= arrf12[4] * f5 * f4;
            f2 -= arrf12[5] * f5 * f3;
            f2 -= 0.5f * arrf12[6] * f3 * f3;
            f2 -= 0.5f * arrf12[7] * f4 * f4;
            if ((f2 -= 0.5f * arrf12[8] * f5 * f5) <= f && n7 != 0) continue;
            f = f2;
            n12 = n7;
        }
        this.gain[0] = 0.015625f * (float)this.gain_cdbk[n12 * 3] + 0.5f;
        this.gain[1] = 0.015625f * (float)this.gain_cdbk[n12 * 3 + 1] + 0.5f;
        this.gain[2] = 0.015625f * (float)this.gain_cdbk[n12 * 3 + 2] + 0.5f;
        arrn[0] = n12;
        for (n7 = 0; n7 < n4; ++n7) {
            arrf5[n + n7] = this.gain[0] * this.e[2][n7] + this.gain[1] * this.e[1][n7] + this.gain[2] * this.e[0][n7];
        }
        float f6 = 0.0f;
        float f7 = 0.0f;
        for (n7 = 0; n7 < n4; ++n7) {
            f6 += arrf[n7] * arrf[n7];
        }
        for (n7 = 0; n7 < n4; ++n7) {
            f7 += (arrf[n7] - this.gain[2] * arrf10[0][n7] - this.gain[1] * arrf10[1][n7] - this.gain[0] * arrf10[2][n7]) * (arrf[n7] - this.gain[2] * arrf10[0][n7] - this.gain[1] * arrf10[1][n7] - this.gain[0] * arrf10[2][n7]);
        }
        return f7;
    }
}


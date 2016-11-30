/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class Filters {
    private int last_pitch;
    private float[] last_pitch_gain = new float[3];
    private float smooth_gain;
    private float[] xx = new float[1024];

    public void init() {
        this.last_pitch = 0;
        this.last_pitch_gain[2] = 0.0f;
        this.last_pitch_gain[1] = 0.0f;
        this.last_pitch_gain[0] = 0.0f;
        this.smooth_gain = 1.0f;
    }

    public static final void bw_lpc(float f, float[] arrf, float[] arrf2, int n) {
        float f2 = 1.0f;
        for (int i = 0; i < n + 1; ++i) {
            arrf2[i] = f2 * arrf[i];
            f2 *= f;
        }
    }

    public static final void filter_mem2(float[] arrf, int n, float[] arrf2, float[] arrf3, int n2, int n3, float[] arrf4, int n4) {
        for (int i = 0; i < n2; ++i) {
            float f = arrf[n + i];
            arrf[n + i] = arrf2[0] * f + arrf4[n4 + 0];
            float f2 = arrf[n + i];
            for (int j = 0; j < n3 - 1; ++j) {
                arrf4[n4 + j] = arrf4[n4 + j + 1] + arrf2[j + 1] * f - arrf3[j + 1] * f2;
            }
            arrf4[n4 + n3 - 1] = arrf2[n3] * f - arrf3[n3] * f2;
        }
    }

    public static final void filter_mem2(float[] arrf, int n, float[] arrf2, float[] arrf3, float[] arrf4, int n2, int n3, int n4, float[] arrf5, int n5) {
        for (int i = 0; i < n3; ++i) {
            float f = arrf[n + i];
            arrf4[n2 + i] = arrf2[0] * f + arrf5[0];
            float f2 = arrf4[n2 + i];
            for (int j = 0; j < n4 - 1; ++j) {
                arrf5[n5 + j] = arrf5[n5 + j + 1] + arrf2[j + 1] * f - arrf3[j + 1] * f2;
            }
            arrf5[n5 + n4 - 1] = arrf2[n4] * f - arrf3[n4] * f2;
        }
    }

    public static final void iir_mem2(float[] arrf, int n, float[] arrf2, float[] arrf3, int n2, int n3, int n4, float[] arrf4) {
        for (int i = 0; i < n3; ++i) {
            arrf3[n2 + i] = arrf[n + i] + arrf4[0];
            for (int j = 0; j < n4 - 1; ++j) {
                arrf4[j] = arrf4[j + 1] - arrf2[j + 1] * arrf3[n2 + i];
            }
            arrf4[n4 - 1] = (- arrf2[n4]) * arrf3[n2 + i];
        }
    }

    public static final void fir_mem2(float[] arrf, int n, float[] arrf2, float[] arrf3, int n2, int n3, int n4, float[] arrf4) {
        for (int i = 0; i < n3; ++i) {
            float f = arrf[n + i];
            arrf3[n2 + i] = arrf2[0] * f + arrf4[0];
            for (int j = 0; j < n4 - 1; ++j) {
                arrf4[j] = arrf4[j + 1] + arrf2[j + 1] * f;
            }
            arrf4[n4 - 1] = arrf2[n4] * f;
        }
    }

    public static final void syn_percep_zero(float[] arrf, int n, float[] arrf2, float[] arrf3, float[] arrf4, float[] arrf5, int n2, int n3) {
        float[] arrf6 = new float[n3];
        Filters.filter_mem2(arrf, n, arrf3, arrf2, arrf5, 0, n2, n3, arrf6, 0);
        for (int i = 0; i < n3; ++i) {
            arrf6[i] = 0.0f;
        }
        Filters.iir_mem2(arrf5, 0, arrf4, arrf5, 0, n2, n3, arrf6);
    }

    public static final void residue_percep_zero(float[] arrf, int n, float[] arrf2, float[] arrf3, float[] arrf4, float[] arrf5, int n2, int n3) {
        float[] arrf6 = new float[n3];
        Filters.filter_mem2(arrf, n, arrf2, arrf3, arrf5, 0, n2, n3, arrf6, 0);
        for (int i = 0; i < n3; ++i) {
            arrf6[i] = 0.0f;
        }
        Filters.fir_mem2(arrf5, 0, arrf4, arrf5, 0, n2, n3, arrf6);
    }

    public void fir_mem_up(float[] arrf, float[] arrf2, float[] arrf3, int n, int n2, float[] arrf4) {
        int n3;
        for (n3 = 0; n3 < n / 2; ++n3) {
            this.xx[2 * n3] = arrf[n / 2 - 1 - n3];
        }
        for (n3 = 0; n3 < n2 - 1; n3 += 2) {
            this.xx[n + n3] = arrf4[n3 + 1];
        }
        for (n3 = 0; n3 < n; n3 += 4) {
            float f = 0.0f;
            float f2 = 0.0f;
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = this.xx[n - 4 - n3];
            for (int i = 0; i < n2; i += 4) {
                float f6 = arrf2[i];
                float f7 = arrf2[i + 1];
                float f8 = this.xx[n - 2 + i - n3];
                f4 += f6 * f8;
                f3 += f7 * f8;
                f2 += f6 * f5;
                f += f7 * f5;
                f6 = arrf2[i + 2];
                f7 = arrf2[i + 3];
                f5 = this.xx[n + i - n3];
                f4 += f6 * f5;
                f3 += f7 * f5;
                f2 += f6 * f8;
                f += f7 * f8;
            }
            arrf3[n3] = f4;
            arrf3[n3 + 1] = f3;
            arrf3[n3 + 2] = f2;
            arrf3[n3 + 3] = f;
        }
        for (n3 = 0; n3 < n2 - 1; n3 += 2) {
            arrf4[n3 + 1] = this.xx[n3];
        }
    }

    public void comb_filter(float[] arrf, int n, float[] arrf2, int n2, int n3, int n4, float[] arrf3, float f) {
        int n5;
        float f2 = 0.0f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        for (n5 = n; n5 < n + n3; ++n5) {
            f2 += arrf[n5] * arrf[n5];
        }
        f4 = 0.5f * Math.abs(arrf3[0] + arrf3[1] + arrf3[2] + this.last_pitch_gain[0] + this.last_pitch_gain[1] + this.last_pitch_gain[2]);
        if (f4 > 1.3f) {
            f *= 1.3f / f4;
        }
        if (f4 < 0.5f) {
            f *= 2.0f * f4;
        }
        float f5 = 1.0f / (float)n3;
        float f6 = 0.0f;
        n5 = 0;
        int n6 = n;
        while (n5 < n3) {
            arrf2[n2 + n5] = arrf[n6] + f * f6 * (arrf3[0] * arrf[n6 - n4 + 1] + arrf3[1] * arrf[n6 - n4] + arrf3[2] * arrf[n6 - n4 - 1]) + f * (1.0f - (f6 += f5)) * (this.last_pitch_gain[0] * arrf[n6 - this.last_pitch + 1] + this.last_pitch_gain[1] * arrf[n6 - this.last_pitch] + this.last_pitch_gain[2] * arrf[n6 - this.last_pitch - 1]);
            ++n5;
            ++n6;
        }
        this.last_pitch_gain[0] = arrf3[0];
        this.last_pitch_gain[1] = arrf3[1];
        this.last_pitch_gain[2] = arrf3[2];
        this.last_pitch = n4;
        for (n5 = n2; n5 < n2 + n3; ++n5) {
            f3 += arrf2[n5] * arrf2[n5];
        }
        float f7 = (float)Math.sqrt(f2 / (0.1f + f3));
        if (f7 < 0.5f) {
            f7 = 0.5f;
        }
        if (f7 > 1.0f) {
            f7 = 1.0f;
        }
        n5 = n2;
        while (n5 < n2 + n3) {
            this.smooth_gain = 0.96f * this.smooth_gain + 0.04f * f7;
            float[] arrf4 = arrf2;
            int n7 = n5++;
            arrf4[n7] = arrf4[n7] * this.smooth_gain;
        }
    }

    public static final void qmf_decomp(float[] arrf, float[] arrf2, float[] arrf3, float[] arrf4, int n, int n2, float[] arrf5) {
        int n3;
        float[] arrf6 = new float[n2];
        float[] arrf7 = new float[n + n2 - 1];
        int n4 = n2 - 1;
        int n5 = n2 >> 1;
        for (n3 = 0; n3 < n2; ++n3) {
            arrf6[n2 - n3 - 1] = arrf2[n3];
        }
        for (n3 = 0; n3 < n2 - 1; ++n3) {
            arrf7[n3] = arrf5[n2 - n3 - 2];
        }
        for (n3 = 0; n3 < n; ++n3) {
            arrf7[n3 + n2 - 1] = arrf[n3];
        }
        n3 = 0;
        int n6 = 0;
        while (n3 < n) {
            arrf3[n6] = 0.0f;
            arrf4[n6] = 0.0f;
            for (int i = 0; i < n5; ++i) {
                float[] arrf8 = arrf3;
                int n7 = n6;
                arrf8[n7] = arrf8[n7] + arrf6[i] * (arrf7[n3 + i] + arrf7[n4 + n3 - i]);
                float[] arrf9 = arrf4;
                int n8 = n6;
                arrf9[n8] = arrf9[n8] - arrf6[i] * (arrf7[n3 + i] - arrf7[n4 + n3 - i]);
                float[] arrf10 = arrf3;
                int n9 = n6;
                arrf10[n9] = arrf10[n9] + arrf6[i] * (arrf7[n3 + i] + arrf7[n4 + n3 - ++i]);
                float[] arrf11 = arrf4;
                int n10 = n6;
                arrf11[n10] = arrf11[n10] + arrf6[i] * (arrf7[n3 + i] - arrf7[n4 + n3 - i]);
            }
            n3 += 2;
            ++n6;
        }
        for (n3 = 0; n3 < n2 - 1; ++n3) {
            arrf5[n3] = arrf[n - n3 - 1];
        }
    }
}


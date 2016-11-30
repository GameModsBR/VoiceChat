/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class Lsp {
    private float[] pw = new float[42];

    public static final float cheb_poly_eva(float[] arrf, float f, int n) {
        int n2 = n >> 1;
        float[] arrf2 = new float[n2 + 1];
        arrf2[0] = 1.0f;
        arrf2[1] = f;
        float f2 = arrf[n2] + arrf[n2 - 1] * f;
        f *= 2.0f;
        for (int i = 2; i <= n2; ++i) {
            arrf2[i] = f * arrf2[i - 1] - arrf2[i - 2];
            f2 += arrf[n2 - i] * arrf2[i];
        }
        return f2;
    }

    public static int lpc2lsp(float[] arrf, int n, float[] arrf2, int n2, float f) {
        int n3;
        float f2 = 0.0f;
        int n4 = 0;
        boolean bl = true;
        int n5 = n / 2;
        float[] arrf3 = new float[n5 + 1];
        float[] arrf4 = new float[n5 + 1];
        int n6 = 0;
        int n7 = 0;
        int n8 = n6;
        int n9 = n7;
        arrf4[n6++] = 1.0f;
        arrf3[n7++] = 1.0f;
        for (n3 = 1; n3 <= n5; ++n3) {
            arrf4[n6++] = arrf[n3] + arrf[n + 1 - n3] - arrf4[n8++];
            arrf3[n7++] = arrf[n3] - arrf[n + 1 - n3] + arrf3[n9++];
        }
        n6 = 0;
        n7 = 0;
        for (n3 = 0; n3 < n5; ++n3) {
            arrf4[n6] = 2.0f * arrf4[n6];
            arrf3[n7] = 2.0f * arrf3[n7];
            ++n6;
            ++n7;
        }
        n6 = 0;
        n7 = 0;
        float f3 = 0.0f;
        float f4 = 1.0f;
        for (int i = 0; i < n; ++i) {
            float[] arrf5 = i % 2 != 0 ? arrf3 : arrf4;
            float f5 = Lsp.cheb_poly_eva(arrf5, f4, n);
            bl = true;
            while (bl && (double)f3 >= -1.0) {
                float f6;
                float f7 = (float)((double)f * (1.0 - 0.9 * (double)f4 * (double)f4));
                if ((double)Math.abs(f5) < 0.2) {
                    f7 = (float)((double)f7 * 0.5);
                }
                f3 = f4 - f7;
                float f8 = f6 = Lsp.cheb_poly_eva(arrf5, f3, n);
                float f9 = f3;
                if ((double)(f6 * f5) < 0.0) {
                    ++n4;
                    float f10 = f5;
                    for (int j = 0; j <= n2; ++j) {
                        f2 = (f4 + f3) / 2.0f;
                        f10 = Lsp.cheb_poly_eva(arrf5, f2, n);
                        if ((double)(f10 * f5) > 0.0) {
                            f5 = f10;
                            f4 = f2;
                            continue;
                        }
                        f6 = f10;
                        f3 = f2;
                    }
                    arrf2[i] = f2;
                    f4 = f2;
                    bl = false;
                    continue;
                }
                f5 = f8;
                f4 = f9;
            }
        }
        return n4;
    }

    public void lsp2lpc(float[] arrf, float[] arrf2, int n) {
        int n2;
        int n3 = 0;
        int n4 = n / 2;
        for (n2 = 0; n2 < 4 * n4 + 2; ++n2) {
            this.pw[n2] = 0.0f;
        }
        float f = 1.0f;
        float f2 = 1.0f;
        for (int i = 0; i <= n; ++i) {
            float f3;
            float f4;
            int n5 = 0;
            n2 = 0;
            while (n2 < n4) {
                int n6 = n2 * 4;
                int n7 = n6 + 1;
                int n8 = n7 + 1;
                n3 = n8 + 1;
                f3 = f - 2.0f * arrf[n5] * this.pw[n6] + this.pw[n7];
                f4 = f2 - 2.0f * arrf[n5 + 1] * this.pw[n8] + this.pw[n3];
                this.pw[n7] = this.pw[n6];
                this.pw[n3] = this.pw[n8];
                this.pw[n6] = f;
                this.pw[n8] = f2;
                f = f3;
                f2 = f4;
                ++n2;
                n5 += 2;
            }
            f3 = f + this.pw[n3 + 1];
            f4 = f2 - this.pw[n3 + 2];
            arrf2[i] = (f3 + f4) * 0.5f;
            this.pw[n3 + 1] = f;
            this.pw[n3 + 2] = f2;
            f = 0.0f;
            f2 = 0.0f;
        }
    }

    public static void enforce_margin(float[] arrf, int n, float f) {
        if (arrf[0] < f) {
            arrf[0] = f;
        }
        if (arrf[n - 1] > 3.1415927f - f) {
            arrf[n - 1] = 3.1415927f - f;
        }
        for (int i = 1; i < n - 1; ++i) {
            if (arrf[i] < arrf[i - 1] + f) {
                arrf[i] = arrf[i - 1] + f;
            }
            if (arrf[i] <= arrf[i + 1] - f) continue;
            arrf[i] = 0.5f * (arrf[i] + arrf[i + 1] - f);
        }
    }
}


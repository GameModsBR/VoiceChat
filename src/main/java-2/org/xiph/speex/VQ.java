/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class VQ {
    public static final int index(float f, float[] arrf, int n) {
        float f2 = 0.0f;
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            float f3 = f - arrf[i];
            f3 *= f3;
            if (i != 0 && f3 >= f2) continue;
            f2 = f3;
            n2 = i;
        }
        return n2;
    }

    public static final int index(float[] arrf, float[] arrf2, int n, int n2) {
        int n3 = 0;
        float f = 0.0f;
        int n4 = 0;
        for (int i = 0; i < n2; ++i) {
            float f2 = 0.0f;
            for (int j = 0; j < n; ++j) {
                float f3 = arrf[j] - arrf2[n3++];
                f2 += f3 * f3;
            }
            if (i != 0 && f2 >= f) continue;
            f = f2;
            n4 = i;
        }
        return n4;
    }

    public static final void nbest(float[] arrf, int n, float[] arrf2, int n2, int n3, float[] arrf3, int n4, int[] arrn, float[] arrf4) {
        int n5 = 0;
        int n6 = 0;
        for (int i = 0; i < n3; ++i) {
            float f = 0.5f * arrf3[i];
            for (int j = 0; j < n2; ++j) {
                f -= arrf[n + j] * arrf2[n5++];
            }
            if (i >= n4 && f >= arrf4[n4 - 1]) continue;
            for (int k = n4 - 1; k >= 1 && (k > n6 || f < arrf4[k - 1]); --k) {
                arrf4[k] = arrf4[k - 1];
                arrn[k] = arrn[k - 1];
            }
            arrf4[k] = f;
            arrn[k] = i;
            ++n6;
        }
    }

    public static final void nbest_sign(float[] arrf, int n, float[] arrf2, int n2, int n3, float[] arrf3, int n4, int[] arrn, float[] arrf4) {
        int n5 = 0;
        int n6 = 0;
        for (int i = 0; i < n3; ++i) {
            boolean bl;
            int n7;
            float f = 0.0f;
            for (int j = 0; j < n2; ++j) {
                f -= arrf[n + j] * arrf2[n5++];
            }
            if (f > 0.0f) {
                bl = true;
                f = - f;
            } else {
                bl = false;
            }
            f = (float)((double)f + 0.5 * (double)arrf3[i]);
            if (i >= n4 && f >= arrf4[n4 - 1]) continue;
            for (n7 = n4 - 1; n7 >= 1 && (n7 > n6 || f < arrf4[n7 - 1]); --n7) {
                arrf4[n7] = arrf4[n7 - 1];
                arrn[n7] = arrn[n7 - 1];
            }
            arrf4[n7] = f;
            arrn[n7] = i;
            ++n6;
            if (!bl) continue;
            int[] arrn2 = arrn;
            int n8 = n7;
            arrn2[n8] = arrn2[n8] + n3;
        }
    }
}


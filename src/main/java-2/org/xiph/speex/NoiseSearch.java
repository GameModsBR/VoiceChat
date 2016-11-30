/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.CbSearch;
import org.xiph.speex.Filters;

public class NoiseSearch
extends CbSearch {
    public final void quant(float[] arrf, float[] arrf2, float[] arrf3, float[] arrf4, int n, int n2, float[] arrf5, int n3, float[] arrf6, Bits bits, int n4) {
        int n5;
        float[] arrf7 = new float[n2];
        Filters.residue_percep_zero(arrf, 0, arrf2, arrf3, arrf4, arrf7, n2, n);
        for (n5 = 0; n5 < n2; ++n5) {
            float[] arrf8 = arrf5;
            int n6 = n3 + n5;
            arrf8[n6] = arrf8[n6] + arrf7[n5];
        }
        for (n5 = 0; n5 < n2; ++n5) {
            arrf[n5] = 0.0f;
        }
    }

    public final void unquant(float[] arrf, int n, int n2, Bits bits) {
        for (int i = 0; i < n2; ++i) {
            float[] arrf2 = arrf;
            int n3 = n + i;
            arrf2[n3] = arrf2[n3] + (float)(3.0 * (Math.random() - 0.5));
        }
    }
}


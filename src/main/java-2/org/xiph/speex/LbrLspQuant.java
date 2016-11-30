/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.Codebook;
import org.xiph.speex.LspQuant;

public class LbrLspQuant
extends LspQuant {
    public final void quant(float[] arrf, float[] arrf2, int n, Bits bits) {
        int n2;
        float[] arrf3 = new float[20];
        for (n2 = 0; n2 < n; ++n2) {
            arrf2[n2] = arrf[n2];
        }
        arrf3[0] = 1.0f / (arrf2[1] - arrf2[0]);
        arrf3[n - 1] = 1.0f / (arrf2[n - 1] - arrf2[n - 2]);
        for (n2 = 1; n2 < n - 1; ++n2) {
            float f = 1.0f / ((0.15f + arrf2[n2] - arrf2[n2 - 1]) * (0.15f + arrf2[n2] - arrf2[n2 - 1]));
            float f2 = 1.0f / ((0.15f + arrf2[n2 + 1] - arrf2[n2]) * (0.15f + arrf2[n2 + 1] - arrf2[n2]));
            arrf3[n2] = f > f2 ? f : f2;
        }
        for (n2 = 0; n2 < n; ++n2) {
            float[] arrf4 = arrf2;
            int n3 = n2;
            arrf4[n3] = (float)((double)arrf4[n3] - (0.25 * (double)n2 + 0.25));
        }
        n2 = 0;
        while (n2 < n) {
            float[] arrf5 = arrf2;
            int n4 = n2++;
            arrf5[n4] = arrf5[n4] * 256.0f;
        }
        int n5 = LbrLspQuant.lsp_quant(arrf2, 0, Codebook.cdbk_nb, 64, n);
        bits.pack(n5, 6);
        n2 = 0;
        while (n2 < n) {
            float[] arrf6 = arrf2;
            int n6 = n2++;
            arrf6[n6] = arrf6[n6] * 2.0f;
        }
        n5 = LbrLspQuant.lsp_weight_quant(arrf2, 0, arrf3, 0, Codebook.cdbk_nb_low1, 64, 5);
        bits.pack(n5, 6);
        n5 = LbrLspQuant.lsp_weight_quant(arrf2, 5, arrf3, 5, Codebook.cdbk_nb_high1, 64, 5);
        bits.pack(n5, 6);
        n2 = 0;
        while (n2 < n) {
            float[] arrf7 = arrf2;
            int n7 = n2++;
            arrf7[n7] = (float)((double)arrf7[n7] * 0.0019531);
        }
        for (n2 = 0; n2 < n; ++n2) {
            arrf2[n2] = arrf[n2] - arrf2[n2];
        }
    }

    public final void unquant(float[] arrf, int n, Bits bits) {
        for (int i = 0; i < n; ++i) {
            arrf[i] = 0.25f * (float)i + 0.25f;
        }
        this.unpackPlus(arrf, Codebook.cdbk_nb, bits, 0.0039062f, 10, 0);
        this.unpackPlus(arrf, Codebook.cdbk_nb_low1, bits, 0.0019531f, 5, 0);
        this.unpackPlus(arrf, Codebook.cdbk_nb_high1, bits, 0.0019531f, 5, 5);
    }
}


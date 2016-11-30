/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.Ltp;

public class LtpForcedPitch
extends Ltp {
    public final int quant(float[] arrf, float[] arrf2, int n, float[] arrf3, float[] arrf4, float[] arrf5, float[] arrf6, int n2, int n3, int n4, float f, int n5, int n6, Bits bits, float[] arrf7, int n7, float[] arrf8, int n8) {
        if (f > 0.99f) {
            f = 0.99f;
        }
        for (int i = 0; i < n6; ++i) {
            arrf6[n2 + i] = arrf6[n2 + i - n3] * f;
        }
        return n3;
    }

    public final int unquant(float[] arrf, int n, int n2, float f, int n3, float[] arrf2, Bits bits, int n4, int n5, float f2) {
        if (f > 0.99f) {
            f = 0.99f;
        }
        for (int i = 0; i < n3; ++i) {
            arrf[n + i] = arrf[n + i - n2] * f;
        }
        arrf2[2] = 0.0f;
        arrf2[0] = 0.0f;
        arrf2[1] = f;
        return n2;
    }
}


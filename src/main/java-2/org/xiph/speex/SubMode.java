/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.CbSearch;
import org.xiph.speex.LspQuant;
import org.xiph.speex.Ltp;

public class SubMode {
    public int lbr_pitch;
    public int forced_pitch_gain;
    public int have_subframe_gain;
    public int double_codebook;
    public LspQuant lsqQuant;
    public Ltp ltp;
    public CbSearch innovation;
    public float lpc_enh_k1;
    public float lpc_enh_k2;
    public float comb_gain;
    public int bits_per_frame;

    public SubMode(int n, int n2, int n3, int n4, LspQuant lspQuant, Ltp ltp, CbSearch cbSearch, float f, float f2, float f3, int n5) {
        this.lbr_pitch = n;
        this.forced_pitch_gain = n2;
        this.have_subframe_gain = n3;
        this.double_codebook = n4;
        this.lsqQuant = lspQuant;
        this.ltp = ltp;
        this.innovation = cbSearch;
        this.lpc_enh_k1 = f;
        this.lpc_enh_k2 = f2;
        this.comb_gain = f3;
        this.bits_per_frame = n5;
    }
}


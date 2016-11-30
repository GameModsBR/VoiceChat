/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.CbSearch;
import org.xiph.speex.Codebook;
import org.xiph.speex.HighLspQuant;
import org.xiph.speex.LspQuant;
import org.xiph.speex.Ltp;
import org.xiph.speex.NbCodec;
import org.xiph.speex.SplitShapeSearch;
import org.xiph.speex.SubMode;

public class SbCodec
extends NbCodec {
    public static final int[] SB_FRAME_SIZE = new int[]{4, 36, 112, 192, 352, -1, -1, -1};
    public static final int SB_SUBMODES = 8;
    public static final int SB_SUBMODE_BITS = 3;
    public static final int QMF_ORDER = 64;
    protected int fullFrameSize;
    protected float foldingGain;
    protected float[] high;
    protected float[] y0;
    protected float[] y1;
    protected float[] x0d;
    protected float[] g0_mem;
    protected float[] g1_mem;

    public void wbinit() {
        this.submodes = SbCodec.buildWbSubModes();
        this.submodeID = 3;
    }

    public void uwbinit() {
        this.submodes = SbCodec.buildUwbSubModes();
        this.submodeID = 1;
    }

    protected void init(int n, int n2, int n3, int n4, float f) {
        super.init(n, n2, n3, n4);
        this.fullFrameSize = 2 * n;
        this.foldingGain = f;
        this.lag_factor = 0.002f;
        this.high = new float[this.fullFrameSize];
        this.y0 = new float[this.fullFrameSize];
        this.y1 = new float[this.fullFrameSize];
        this.x0d = new float[n];
        this.g0_mem = new float[64];
        this.g1_mem = new float[64];
    }

    protected static SubMode[] buildWbSubModes() {
        HighLspQuant highLspQuant = new HighLspQuant();
        SplitShapeSearch splitShapeSearch = new SplitShapeSearch(40, 10, 4, Codebook.hexc_10_32_table, 5, 0);
        SplitShapeSearch splitShapeSearch2 = new SplitShapeSearch(40, 8, 5, Codebook.hexc_table, 7, 1);
        SubMode[] arrsubMode = new SubMode[8];
        arrsubMode[1] = new SubMode(0, 0, 1, 0, highLspQuant, null, null, 0.75f, 0.75f, -1.0f, 36);
        arrsubMode[2] = new SubMode(0, 0, 1, 0, highLspQuant, null, splitShapeSearch, 0.85f, 0.6f, -1.0f, 112);
        arrsubMode[3] = new SubMode(0, 0, 1, 0, highLspQuant, null, splitShapeSearch2, 0.75f, 0.7f, -1.0f, 192);
        arrsubMode[4] = new SubMode(0, 0, 1, 1, highLspQuant, null, splitShapeSearch2, 0.75f, 0.75f, -1.0f, 352);
        return arrsubMode;
    }

    protected static SubMode[] buildUwbSubModes() {
        HighLspQuant highLspQuant = new HighLspQuant();
        SubMode[] arrsubMode = new SubMode[8];
        arrsubMode[1] = new SubMode(0, 0, 1, 0, highLspQuant, null, null, 0.75f, 0.75f, -1.0f, 2);
        return arrsubMode;
    }

    public int getFrameSize() {
        return this.fullFrameSize;
    }

    public boolean getDtx() {
        return this.dtx_enabled != 0;
    }

    public float[] getExc() {
        float[] arrf = new float[this.fullFrameSize];
        for (int i = 0; i < this.frameSize; ++i) {
            arrf[2 * i] = 2.0f * this.excBuf[this.excIdx + i];
        }
        return arrf;
    }

    public float[] getInnov() {
        return this.getExc();
    }
}


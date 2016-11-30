/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.CbSearch;
import org.xiph.speex.Codebook;
import org.xiph.speex.Filters;
import org.xiph.speex.LbrLspQuant;
import org.xiph.speex.Lsp;
import org.xiph.speex.LspQuant;
import org.xiph.speex.Ltp;
import org.xiph.speex.Ltp3Tap;
import org.xiph.speex.LtpForcedPitch;
import org.xiph.speex.NbLspQuant;
import org.xiph.speex.NoiseSearch;
import org.xiph.speex.SplitShapeSearch;
import org.xiph.speex.SubMode;

public class NbCodec
implements Codebook {
    public static final float VERY_SMALL = 0.0f;
    public static final int[] NB_FRAME_SIZE = new int[]{5, 43, 119, 160, 220, 300, 364, 492, 79, 1, 1, 1, 1, 1, 1, 1};
    public static final int NB_SUBMODES = 16;
    public static final int NB_SUBMODE_BITS = 4;
    public static final float[] exc_gain_quant_scal1 = new float[]{-0.35f, 0.05f};
    public static final float[] exc_gain_quant_scal3 = new float[]{-2.79475f, -1.81066f, -1.16985f, -0.848119f, -0.58719f, -0.329818f, -0.063266f, 0.282826f};
    protected Lsp m_lsp = new Lsp();
    protected Filters filters = new Filters();
    protected SubMode[] submodes;
    protected int submodeID;
    protected int first;
    protected int frameSize;
    protected int subframeSize;
    protected int nbSubframes;
    protected int windowSize;
    protected int lpcSize;
    protected int bufSize;
    protected int min_pitch;
    protected int max_pitch;
    protected float gamma1;
    protected float gamma2;
    protected float lag_factor;
    protected float lpc_floor;
    protected float preemph;
    protected float pre_mem;
    protected float[] frmBuf;
    protected int frmIdx;
    protected float[] excBuf;
    protected int excIdx;
    protected float[] innov;
    protected float[] lpc;
    protected float[] qlsp;
    protected float[] old_qlsp;
    protected float[] interp_qlsp;
    protected float[] interp_qlpc;
    protected float[] mem_sp;
    protected float[] pi_gain;
    protected float[] awk1;
    protected float[] awk2;
    protected float[] awk3;
    protected float voc_m1;
    protected float voc_m2;
    protected float voc_mean;
    protected int voc_offset;
    protected int dtx_enabled;

    public void nbinit() {
        this.submodes = NbCodec.buildNbSubModes();
        this.submodeID = 5;
        this.init(160, 40, 10, 640);
    }

    protected void init(int n, int n2, int n3, int n4) {
        this.first = 1;
        this.frameSize = n;
        this.windowSize = n * 3 / 2;
        this.subframeSize = n2;
        this.nbSubframes = n / n2;
        this.lpcSize = n3;
        this.bufSize = n4;
        this.min_pitch = 17;
        this.max_pitch = 144;
        this.preemph = 0.0f;
        this.pre_mem = 0.0f;
        this.gamma1 = 0.9f;
        this.gamma2 = 0.6f;
        this.lag_factor = 0.01f;
        this.lpc_floor = 1.0001f;
        this.frmBuf = new float[n4];
        this.frmIdx = n4 - this.windowSize;
        this.excBuf = new float[n4];
        this.excIdx = n4 - this.windowSize;
        this.innov = new float[n];
        this.lpc = new float[n3 + 1];
        this.qlsp = new float[n3];
        this.old_qlsp = new float[n3];
        this.interp_qlsp = new float[n3];
        this.interp_qlpc = new float[n3 + 1];
        this.mem_sp = new float[5 * n3];
        this.pi_gain = new float[this.nbSubframes];
        this.awk1 = new float[n3 + 1];
        this.awk2 = new float[n3 + 1];
        this.awk3 = new float[n3 + 1];
        this.voc_mean = 0.0f;
        this.voc_m2 = 0.0f;
        this.voc_m1 = 0.0f;
        this.voc_offset = 0;
        this.dtx_enabled = 0;
    }

    private static SubMode[] buildNbSubModes() {
        Ltp3Tap ltp3Tap = new Ltp3Tap(Codebook.gain_cdbk_nb, 7, 7);
        Ltp3Tap ltp3Tap2 = new Ltp3Tap(Codebook.gain_cdbk_lbr, 5, 0);
        Ltp3Tap ltp3Tap3 = new Ltp3Tap(Codebook.gain_cdbk_lbr, 5, 7);
        Ltp3Tap ltp3Tap4 = new Ltp3Tap(Codebook.gain_cdbk_lbr, 5, 7);
        LtpForcedPitch ltpForcedPitch = new LtpForcedPitch();
        NoiseSearch noiseSearch = new NoiseSearch();
        SplitShapeSearch splitShapeSearch = new SplitShapeSearch(40, 10, 4, Codebook.exc_10_16_table, 4, 0);
        SplitShapeSearch splitShapeSearch2 = new SplitShapeSearch(40, 10, 4, Codebook.exc_10_32_table, 5, 0);
        SplitShapeSearch splitShapeSearch3 = new SplitShapeSearch(40, 5, 8, Codebook.exc_5_64_table, 6, 0);
        SplitShapeSearch splitShapeSearch4 = new SplitShapeSearch(40, 8, 5, Codebook.exc_8_128_table, 7, 0);
        SplitShapeSearch splitShapeSearch5 = new SplitShapeSearch(40, 5, 8, Codebook.exc_5_256_table, 8, 0);
        SplitShapeSearch splitShapeSearch6 = new SplitShapeSearch(40, 20, 2, Codebook.exc_20_32_table, 5, 0);
        NbLspQuant nbLspQuant = new NbLspQuant();
        LbrLspQuant lbrLspQuant = new LbrLspQuant();
        SubMode[] arrsubMode = new SubMode[16];
        arrsubMode[1] = new SubMode(0, 1, 0, 0, lbrLspQuant, ltpForcedPitch, noiseSearch, 0.7f, 0.7f, -1.0f, 43);
        arrsubMode[2] = new SubMode(0, 0, 0, 0, lbrLspQuant, ltp3Tap2, splitShapeSearch, 0.7f, 0.5f, 0.55f, 119);
        arrsubMode[3] = new SubMode(-1, 0, 1, 0, lbrLspQuant, ltp3Tap3, splitShapeSearch2, 0.7f, 0.55f, 0.45f, 160);
        arrsubMode[4] = new SubMode(-1, 0, 1, 0, lbrLspQuant, ltp3Tap4, splitShapeSearch4, 0.7f, 0.63f, 0.35f, 220);
        arrsubMode[5] = new SubMode(-1, 0, 3, 0, nbLspQuant, ltp3Tap, splitShapeSearch3, 0.7f, 0.65f, 0.25f, 300);
        arrsubMode[6] = new SubMode(-1, 0, 3, 0, nbLspQuant, ltp3Tap, splitShapeSearch5, 0.68f, 0.65f, 0.1f, 364);
        arrsubMode[7] = new SubMode(-1, 0, 3, 1, nbLspQuant, ltp3Tap, splitShapeSearch3, 0.65f, 0.65f, -1.0f, 492);
        arrsubMode[8] = new SubMode(0, 1, 0, 0, lbrLspQuant, ltpForcedPitch, splitShapeSearch6, 0.7f, 0.5f, 0.65f, 79);
        return arrsubMode;
    }

    public int getFrameSize() {
        return this.frameSize;
    }

    public boolean getDtx() {
        return this.dtx_enabled != 0;
    }

    public float[] getPiGain() {
        return this.pi_gain;
    }

    public float[] getExc() {
        float[] arrf = new float[this.frameSize];
        System.arraycopy(this.excBuf, this.excIdx, arrf, 0, this.frameSize);
        return arrf;
    }

    public float[] getInnov() {
        return this.innov;
    }
}


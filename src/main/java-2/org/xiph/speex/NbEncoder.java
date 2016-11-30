/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.CbSearch;
import org.xiph.speex.Encoder;
import org.xiph.speex.Filters;
import org.xiph.speex.Lpc;
import org.xiph.speex.Lsp;
import org.xiph.speex.LspQuant;
import org.xiph.speex.Ltp;
import org.xiph.speex.Misc;
import org.xiph.speex.NbCodec;
import org.xiph.speex.NoiseSearch;
import org.xiph.speex.SubMode;
import org.xiph.speex.VQ;
import org.xiph.speex.Vbr;

public class NbEncoder
extends NbCodec
implements Encoder {
    public static final int[] NB_QUALITY_MAP = new int[]{1, 8, 2, 3, 3, 4, 4, 5, 5, 6, 7};
    private int bounded_pitch;
    private int[] pitch;
    private float pre_mem2;
    private float[] exc2Buf;
    private int exc2Idx;
    private float[] swBuf;
    private int swIdx;
    private float[] window;
    private float[] buf2;
    private float[] autocorr;
    private float[] lagWindow;
    private float[] lsp;
    private float[] old_lsp;
    private float[] interp_lsp;
    private float[] interp_lpc;
    private float[] bw_lpc1;
    private float[] bw_lpc2;
    private float[] rc;
    private float[] mem_sw;
    private float[] mem_sw_whole;
    private float[] mem_exc;
    private Vbr vbr;
    private int dtx_count;
    private float[] innov2;
    protected int complexity;
    protected int vbr_enabled;
    protected int vad_enabled;
    protected int abr_enabled;
    protected float vbr_quality;
    protected float relative_quality;
    protected float abr_drift;
    protected float abr_drift2;
    protected float abr_count;
    protected int sampling_rate;
    protected int submodeSelect;

    public void init(int n, int n2, int n3, int n4) {
        super.init(n, n2, n3, n4);
        this.complexity = 3;
        this.vbr_enabled = 0;
        this.vad_enabled = 0;
        this.abr_enabled = 0;
        this.vbr_quality = 8.0f;
        this.submodeSelect = 5;
        this.pre_mem2 = 0.0f;
        this.bounded_pitch = 1;
        this.exc2Buf = new float[n4];
        this.exc2Idx = n4 - this.windowSize;
        this.swBuf = new float[n4];
        this.swIdx = n4 - this.windowSize;
        this.window = Misc.window(this.windowSize, n2);
        this.lagWindow = Misc.lagWindow(n3, this.lag_factor);
        this.autocorr = new float[n3 + 1];
        this.buf2 = new float[this.windowSize];
        this.interp_lpc = new float[n3 + 1];
        this.interp_qlpc = new float[n3 + 1];
        this.bw_lpc1 = new float[n3 + 1];
        this.bw_lpc2 = new float[n3 + 1];
        this.lsp = new float[n3];
        this.qlsp = new float[n3];
        this.old_lsp = new float[n3];
        this.old_qlsp = new float[n3];
        this.interp_lsp = new float[n3];
        this.interp_qlsp = new float[n3];
        this.rc = new float[n3];
        this.mem_sp = new float[n3];
        this.mem_sw = new float[n3];
        this.mem_sw_whole = new float[n3];
        this.mem_exc = new float[n3];
        this.vbr = new Vbr();
        this.dtx_count = 0;
        this.abr_count = 0.0f;
        this.sampling_rate = 8000;
        this.awk1 = new float[n3 + 1];
        this.awk2 = new float[n3 + 1];
        this.awk3 = new float[n3 + 1];
        this.innov2 = new float[40];
        this.filters.init();
        this.pitch = new int[this.nbSubframes];
    }

    public int encode(Bits bits, float[] arrf) {
        int n;
        float f;
        void var14_22;
        reference var14_6;
        float f2;
        int n2;
        int n3;
        System.arraycopy(this.frmBuf, this.frameSize, this.frmBuf, 0, this.bufSize - this.frameSize);
        this.frmBuf[this.bufSize - this.frameSize] = arrf[0] - this.preemph * this.pre_mem;
        for (n3 = 1; n3 < this.frameSize; ++n3) {
            this.frmBuf[this.bufSize - this.frameSize + n3] = arrf[n3] - this.preemph * arrf[n3 - 1];
        }
        this.pre_mem = arrf[this.frameSize - 1];
        System.arraycopy(this.exc2Buf, this.frameSize, this.exc2Buf, 0, this.bufSize - this.frameSize);
        System.arraycopy(this.excBuf, this.frameSize, this.excBuf, 0, this.bufSize - this.frameSize);
        System.arraycopy(this.swBuf, this.frameSize, this.swBuf, 0, this.bufSize - this.frameSize);
        for (n3 = 0; n3 < this.windowSize; ++n3) {
            this.buf2[n3] = this.frmBuf[n3 + this.frmIdx] * this.window[n3];
        }
        Lpc.autocorr(this.buf2, this.autocorr, this.lpcSize + 1, this.windowSize);
        float[] arrf2 = this.autocorr;
        arrf2[0] = arrf2[0] + 10.0f;
        float[] arrf3 = this.autocorr;
        arrf3[0] = arrf3[0] * this.lpc_floor;
        for (n3 = 0; n3 < this.lpcSize + 1; ++n3) {
            float[] arrf4 = this.autocorr;
            int n4 = n3;
            arrf4[n4] = arrf4[n4] * this.lagWindow[n3];
        }
        Lpc.wld(this.lpc, this.autocorr, this.rc, this.lpcSize);
        System.arraycopy(this.lpc, 0, this.lpc, 1, this.lpcSize);
        this.lpc[0] = 1.0f;
        int n5 = Lsp.lpc2lsp(this.lpc, this.lpcSize, this.lsp, 15, 0.2f);
        if (n5 == this.lpcSize) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.lsp[n3] = (float)Math.acos(this.lsp[n3]);
            }
        } else {
            if (this.complexity > 1) {
                n5 = Lsp.lpc2lsp(this.lpc, this.lpcSize, this.lsp, 11, 0.05f);
            }
            if (n5 == this.lpcSize) {
                for (n3 = 0; n3 < this.lpcSize; ++n3) {
                    this.lsp[n3] = (float)Math.acos(this.lsp[n3]);
                }
            } else {
                for (n3 = 0; n3 < this.lpcSize; ++n3) {
                    this.lsp[n3] = this.old_lsp[n3];
                }
            }
        }
        float f3 = 0.0f;
        for (n3 = 0; n3 < this.lpcSize; ++n3) {
            f3 += (this.old_lsp[n3] - this.lsp[n3]) * (this.old_lsp[n3] - this.lsp[n3]);
        }
        if (this.first != 0) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_lsp[n3] = this.lsp[n3];
            }
        } else {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_lsp[n3] = 0.375f * this.old_lsp[n3] + 0.625f * this.lsp[n3];
            }
        }
        Lsp.enforce_margin(this.interp_lsp, this.lpcSize, 0.002f);
        for (n3 = 0; n3 < this.lpcSize; ++n3) {
            this.interp_lsp[n3] = (float)Math.cos(this.interp_lsp[n3]);
        }
        this.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, this.lpcSize);
        if (this.submodes[this.submodeID] == null || this.vbr_enabled != 0 || this.vad_enabled != 0 || this.submodes[this.submodeID].forced_pitch_gain != 0 || this.submodes[this.submodeID].lbr_pitch != -1) {
            var14_6 = new int[6];
            float[] arrf5 = new float[6];
            Filters.bw_lpc(this.gamma1, this.interp_lpc, this.bw_lpc1, this.lpcSize);
            Filters.bw_lpc(this.gamma2, this.interp_lpc, this.bw_lpc2, this.lpcSize);
            Filters.filter_mem2(this.frmBuf, this.frmIdx, this.bw_lpc1, this.bw_lpc2, this.swBuf, this.swIdx, this.frameSize, this.lpcSize, this.mem_sw_whole, 0);
            Ltp.open_loop_nbest_pitch(this.swBuf, this.swIdx, this.min_pitch, this.max_pitch, this.frameSize, var14_6, arrf5, 6);
            n = var14_6[0];
            f = arrf5[0];
            for (n3 = 1; n3 < 6; ++n3) {
                if ((double)arrf5[n3] <= 0.85 * (double)f || Math.abs((double)var14_6[n3] - (double)n / 2.0) > 1.0 && Math.abs((double)var14_6[n3] - (double)n / 3.0) > 1.0 && Math.abs((double)var14_6[n3] - (double)n / 4.0) > 1.0 && Math.abs((double)var14_6[n3] - (double)n / 5.0) > 1.0) continue;
                n = var14_6[n3];
            }
        } else {
            n = 0;
            f = 0.0f;
        }
        Filters.fir_mem2(this.frmBuf, this.frmIdx, this.interp_lpc, this.excBuf, this.excIdx, this.frameSize, this.lpcSize, this.mem_exc);
        float f4 = 0.0f;
        for (n3 = 0; n3 < this.frameSize; ++n3) {
            f4 += this.excBuf[this.excIdx + n3] * this.excBuf[this.excIdx + n3];
        }
        f4 = (float)Math.sqrt(1.0f + f4 / (float)this.frameSize);
        if (this.vbr != null && (this.vbr_enabled != 0 || this.vad_enabled != 0)) {
            if (this.abr_enabled != 0) {
                float f10;
                float f5 = 0.0f;
                if (this.abr_drift2 * this.abr_drift > 0.0f) {
                    float n8;
                    float n6 = -1.0E-5f * this.abr_drift / (1.0f + this.abr_count);
                    if (n6 > 0.05f) {
                        n8 = 0.05f;
                    }
                    if (n8 < -0.05f) {
                        f10 = -0.05f;
                    }
                }
                this.vbr_quality += f10;
                if (this.vbr_quality > 10.0f) {
                    this.vbr_quality = 10.0f;
                }
                if (this.vbr_quality < 0.0f) {
                    this.vbr_quality = 0.0f;
                }
            }
            this.relative_quality = this.vbr.analysis(arrf, this.frameSize, n, f);
            if (this.vbr_enabled != 0) {
                void var14_16;
                void var14_12;
                boolean bl = false;
                f2 = 100.0f;
                int n6 = 8;
                while (--var14_12 > 0) {
                    n2 = (int)Math.floor(this.vbr_quality);
                    float f5 = n2 == 10 ? Vbr.nb_thresh[var14_12][n2] : (this.vbr_quality - (float)n2) * Vbr.nb_thresh[var14_12][n2 + 1] + ((float)(1 + n2) - this.vbr_quality) * Vbr.nb_thresh[var14_12][n2];
                    if (this.relative_quality <= f5 || this.relative_quality - f5 >= f2) continue;
                    bl = var14_12;
                    f2 = this.relative_quality - f5;
                }
                boolean bl2 = bl;
                if (!bl2) {
                    if (this.dtx_count == 0 || (double)f3 > 0.05 || this.dtx_enabled == 0 || this.dtx_count > 20) {
                        boolean bl3 = true;
                        this.dtx_count = 1;
                    } else {
                        boolean bl4 = false;
                        ++this.dtx_count;
                    }
                } else {
                    this.dtx_count = 0;
                }
                this.setMode((int)var14_16);
                if (this.abr_enabled != 0) {
                    n2 = this.getBitRate();
                    this.abr_drift += (float)(n2 - this.abr_enabled);
                    this.abr_drift2 = 0.95f * this.abr_drift2 + 0.05f * (float)(n2 - this.abr_enabled);
                    this.abr_count = (float)((double)this.abr_count + 1.0);
                }
            } else {
                void var14_20;
                if (this.relative_quality < 2.0f) {
                    if (this.dtx_count == 0 || (double)f3 > 0.05 || this.dtx_enabled == 0 || this.dtx_count > 20) {
                        this.dtx_count = 1;
                        boolean bl = true;
                    } else {
                        boolean bl = false;
                        ++this.dtx_count;
                    }
                } else {
                    this.dtx_count = 0;
                    int n7 = this.submodeSelect;
                }
                this.submodeID = var14_20;
            }
        } else {
            this.relative_quality = -1.0f;
        }
        bits.pack(0, 1);
        bits.pack(this.submodeID, 4);
        if (this.submodes[this.submodeID] == null) {
            for (n3 = 0; n3 < this.frameSize; ++n3) {
                this.swBuf[this.swIdx + n3] = 0.0f;
                this.exc2Buf[this.exc2Idx + n3] = 0.0f;
                this.excBuf[this.excIdx + n3] = 0.0f;
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.mem_sw[n3] = 0.0f;
            }
            this.first = 1;
            this.bounded_pitch = 1;
            Filters.iir_mem2(this.excBuf, this.excIdx, this.interp_qlpc, this.frmBuf, this.frmIdx, this.frameSize, this.lpcSize, this.mem_sp);
            arrf[0] = this.frmBuf[this.frmIdx] + this.preemph * this.pre_mem2;
            for (n3 = 1; n3 < this.frameSize; ++n3) {
                this.frmIdx = n3;
                arrf[n3] = this.frmBuf[this.frmIdx] + this.preemph * arrf[n3 - 1];
            }
            this.pre_mem2 = arrf[this.frameSize - 1];
            return 0;
        }
        if (this.first != 0) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.old_lsp[n3] = this.lsp[n3];
            }
        }
        this.submodes[this.submodeID].lsqQuant.quant(this.lsp, this.qlsp, this.lpcSize, bits);
        if (this.submodes[this.submodeID].lbr_pitch != -1) {
            bits.pack(n - this.min_pitch, 7);
        }
        if (this.submodes[this.submodeID].forced_pitch_gain != 0) {
            var14_6 = (reference)Math.floor(0.5 + (double)(15.0f * f));
            if (var14_6 > 15) {
                var14_6 = (reference)15;
            }
            if (var14_6 < 0) {
                var14_6 = (reference)false ? 1 : 0;
            }
            bits.pack((int)var14_6, 4);
            f = 0.066667f * (Object)var14_6;
        }
        if ((var14_6 = (reference)Math.floor(0.5 + 3.5 * Math.log(f4))) < 0) {
            var14_6 = (reference)false ? 1 : 0;
        }
        if (var14_6 > 31) {
            var14_6 = (reference)31;
        }
        f4 = (float)Math.exp((double)((Object)var14_6 / 3.5));
        bits.pack((int)var14_6, 5);
        if (this.first != 0) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.old_qlsp[n3] = this.qlsp[n3];
            }
        }
        float[] arrf6 = new float[this.subframeSize];
        float[] arrf7 = new float[this.subframeSize];
        float[] arrf8 = new float[this.subframeSize];
        float[] arrf9 = new float[this.lpcSize];
        float[] arrf10 = new float[this.frameSize];
        for (n3 = 0; n3 < this.frameSize; ++n3) {
            arrf10[n3] = this.frmBuf[this.frmIdx + n3];
        }
        for (var14_6 = (reference)false ? 1 : 0; var14_6 < this.nbSubframes; ++var14_6) {
            Object object;
            int n8;
            int n9;
            int n10;
            int n11 = this.subframeSize * var14_6;
            n2 = this.frmIdx + n11;
            int n12 = this.excIdx + n11;
            int n13 = this.swIdx + n11;
            int n14 = this.exc2Idx + n11;
            float f6 = (float)(1.0 + (Object)var14_6) / (float)this.nbSubframes;
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_lsp[n3] = (1.0f - f6) * this.old_lsp[n3] + f6 * this.lsp[n3];
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_qlsp[n3] = (1.0f - f6) * this.old_qlsp[n3] + f6 * this.qlsp[n3];
            }
            Lsp.enforce_margin(this.interp_lsp, this.lpcSize, 0.002f);
            Lsp.enforce_margin(this.interp_qlsp, this.lpcSize, 0.002f);
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_lsp[n3] = (float)Math.cos(this.interp_lsp[n3]);
            }
            this.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, this.lpcSize);
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_qlsp[n3] = (float)Math.cos(this.interp_qlsp[n3]);
            }
            this.m_lsp.lsp2lpc(this.interp_qlsp, this.interp_qlpc, this.lpcSize);
            f6 = 1.0f;
            this.pi_gain[var14_6] = 0.0f;
            for (n3 = 0; n3 <= this.lpcSize; ++n3) {
                float[] arrf11 = this.pi_gain;
                reference v5 = var14_6;
                arrf11[v5] = arrf11[v5] + f6 * this.interp_qlpc[n3];
                f6 = - f6;
            }
            Filters.bw_lpc(this.gamma1, this.interp_lpc, this.bw_lpc1, this.lpcSize);
            if (this.gamma2 >= 0.0f) {
                Filters.bw_lpc(this.gamma2, this.interp_lpc, this.bw_lpc2, this.lpcSize);
            } else {
                this.bw_lpc2[0] = 1.0f;
                this.bw_lpc2[1] = - this.preemph;
                for (n3 = 2; n3 <= this.lpcSize; ++n3) {
                    this.bw_lpc2[n3] = 0.0f;
                }
            }
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                this.excBuf[n12 + n3] = 0.0f;
            }
            this.excBuf[n12] = 1.0f;
            Filters.syn_percep_zero(this.excBuf, n12, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, arrf8, this.subframeSize, this.lpcSize);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                this.excBuf[n12 + n3] = 0.0f;
            }
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                this.exc2Buf[n14 + n3] = 0.0f;
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                arrf9[n3] = this.mem_sp[n3];
            }
            Filters.iir_mem2(this.excBuf, n12, this.interp_qlpc, this.excBuf, n12, this.subframeSize, this.lpcSize, arrf9);
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                arrf9[n3] = this.mem_sw[n3];
            }
            Filters.filter_mem2(this.excBuf, n12, this.bw_lpc1, this.bw_lpc2, arrf6, 0, this.subframeSize, this.lpcSize, arrf9, 0);
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                arrf9[n3] = this.mem_sw[n3];
            }
            Filters.filter_mem2(this.frmBuf, n2, this.bw_lpc1, this.bw_lpc2, this.swBuf, n13, this.subframeSize, this.lpcSize, arrf9, 0);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                arrf7[n3] = this.swBuf[n13 + n3] - arrf6[n3];
            }
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                this.exc2Buf[n14 + n3] = 0.0f;
                this.excBuf[n12 + n3] = 0.0f;
            }
            if (this.submodes[this.submodeID].lbr_pitch != -1) {
                object = this.submodes[this.submodeID].lbr_pitch;
                if (object != 0) {
                    if (n < this.min_pitch + object - 1) {
                        n = this.min_pitch + object - 1;
                    }
                    if (n > this.max_pitch - object) {
                        n = this.max_pitch - object;
                    }
                    n8 = n - object + 1;
                    n10 = n + object;
                } else {
                    n8 = n10 = n;
                }
            } else {
                n8 = this.min_pitch;
                n10 = this.max_pitch;
            }
            if (this.bounded_pitch != 0 && n10 > n11) {
                n10 = n11;
            }
            this.pitch[var14_6] = n9 = this.submodes[this.submodeID].ltp.quant(arrf7, this.swBuf, n13, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.excBuf, n12, n8, n10, f, this.lpcSize, this.subframeSize, bits, this.exc2Buf, n14, arrf8, this.complexity);
            Filters.syn_percep_zero(this.excBuf, n12, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, arrf6, this.subframeSize, this.lpcSize);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                float[] arrf12 = arrf7;
                int n15 = n3;
                arrf12[n15] = arrf12[n15] - arrf6[n3];
            }
            float f7 = 0.0f;
            object = var14_6 * this.subframeSize;
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                this.innov[object + n3] = 0.0f;
            }
            Filters.residue_percep_zero(arrf7, 0, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.buf2, this.subframeSize, this.lpcSize);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                f7 += this.buf2[n3] * this.buf2[n3];
            }
            f7 = (float)Math.sqrt(0.1f + f7 / (float)this.subframeSize);
            f7 /= f4;
            if (this.submodes[this.submodeID].have_subframe_gain != 0) {
                int n16;
                f7 = (float)Math.log(f7);
                if (this.submodes[this.submodeID].have_subframe_gain == 3) {
                    n16 = VQ.index(f7, exc_gain_quant_scal3, 8);
                    bits.pack(n16, 3);
                    f7 = exc_gain_quant_scal3[n16];
                } else {
                    n16 = VQ.index(f7, exc_gain_quant_scal1, 2);
                    bits.pack(n16, 1);
                    f7 = exc_gain_quant_scal1[n16];
                }
                f7 = (float)Math.exp(f7);
            } else {
                f7 = 1.0f;
            }
            float f8 = 1.0f / (f7 *= f4);
            n3 = 0;
            while (n3 < this.subframeSize) {
                float[] arrf13 = arrf7;
                int n17 = n3++;
                arrf13[n17] = arrf13[n17] * f8;
            }
            this.submodes[this.submodeID].innovation.quant(arrf7, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.lpcSize, this.subframeSize, this.innov, (int)object, arrf8, bits, this.complexity);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                float[] arrf14 = this.innov;
                reference v11 = object + n3;
                arrf14[v11] = arrf14[v11] * f7;
            }
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                float[] arrf15 = this.excBuf;
                int n18 = n12 + n3;
                arrf15[n18] = arrf15[n18] + this.innov[object + n3];
            }
            if (this.submodes[this.submodeID].double_codebook != 0) {
                float[] arrf16 = new float[this.subframeSize];
                n3 = 0;
                while (n3 < this.subframeSize) {
                    float[] arrf17 = arrf7;
                    int n19 = n3++;
                    arrf17[n19] = (float)((double)arrf17[n19] * 2.2);
                }
                this.submodes[this.submodeID].innovation.quant(arrf7, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.lpcSize, this.subframeSize, arrf16, 0, arrf8, bits, this.complexity);
                n3 = 0;
                while (n3 < this.subframeSize) {
                    float[] arrf18 = arrf16;
                    int n20 = n3++;
                    arrf18[n20] = (float)((double)arrf18[n20] * ((double)f7 * 0.45454545454545453));
                }
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    float[] arrf19 = this.excBuf;
                    int n21 = n12 + n3;
                    arrf19[n21] = arrf19[n21] + arrf16[n3];
                }
            }
            n3 = 0;
            while (n3 < this.subframeSize) {
                float[] arrf20 = arrf7;
                int n22 = n3++;
                arrf20[n22] = arrf20[n22] * f7;
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                arrf9[n3] = this.mem_sp[n3];
            }
            Filters.iir_mem2(this.excBuf, n12, this.interp_qlpc, this.frmBuf, n2, this.subframeSize, this.lpcSize, this.mem_sp);
            Filters.filter_mem2(this.frmBuf, n2, this.bw_lpc1, this.bw_lpc2, this.swBuf, n13, this.subframeSize, this.lpcSize, this.mem_sw, 0);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                this.exc2Buf[n14 + n3] = this.excBuf[n12 + n3];
            }
        }
        if (this.submodeID >= 1) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.old_lsp[n3] = this.lsp[n3];
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.old_qlsp[n3] = this.qlsp[n3];
            }
        }
        if (this.submodeID == 1) {
            if (this.dtx_count != 0) {
                bits.pack(15, 4);
            } else {
                bits.pack(0, 4);
            }
        }
        this.first = 0;
        float f9 = 0.0f;
        float f10 = 0.0f;
        for (n3 = 0; n3 < this.frameSize; ++n3) {
            var14_22 += this.frmBuf[this.frmIdx + n3] * this.frmBuf[this.frmIdx + n3];
            f10 += (this.frmBuf[this.frmIdx + n3] - arrf10[n3]) * (this.frmBuf[this.frmIdx + n3] - arrf10[n3]);
        }
        f2 = (float)(10.0 * Math.log((double)((var14_22 + 1.0f) / (f10 + 1.0f))));
        arrf[0] = this.frmBuf[this.frmIdx] + this.preemph * this.pre_mem2;
        for (n3 = 1; n3 < this.frameSize; ++n3) {
            arrf[n3] = this.frmBuf[this.frmIdx + n3] + this.preemph * arrf[n3 - 1];
        }
        this.pre_mem2 = arrf[this.frameSize - 1];
        this.bounded_pitch = this.submodes[this.submodeID].innovation instanceof NoiseSearch || this.submodeID == 0 ? 1 : 0;
        return 1;
    }

    public int getEncodedFrameSize() {
        return NB_FRAME_SIZE[this.submodeID];
    }

    public void setQuality(int n) {
        if (n < 0) {
            n = 0;
        }
        if (n > 10) {
            n = 10;
        }
        this.submodeID = this.submodeSelect = NB_QUALITY_MAP[n];
    }

    public int getBitRate() {
        if (this.submodes[this.submodeID] != null) {
            return this.sampling_rate * this.submodes[this.submodeID].bits_per_frame / this.frameSize;
        }
        return this.sampling_rate * 5 / this.frameSize;
    }

    public void setMode(int n) {
        if (n < 0) {
            n = 0;
        }
        this.submodeID = this.submodeSelect = n;
    }

    public int getMode() {
        return this.submodeID;
    }

    public void setBitRate(int n) {
        for (int i = 10; i >= 0; --i) {
            this.setQuality(i);
            if (this.getBitRate() > n) continue;
            return;
        }
    }

    public void setVbr(boolean bl) {
        this.vbr_enabled = bl ? 1 : 0;
    }

    public boolean getVbr() {
        return this.vbr_enabled != 0;
    }

    public void setVad(boolean bl) {
        this.vad_enabled = bl ? 1 : 0;
    }

    public boolean getVad() {
        return this.vad_enabled != 0;
    }

    public void setDtx(boolean bl) {
        this.dtx_enabled = bl ? 1 : 0;
    }

    public int getAbr() {
        return this.abr_enabled;
    }

    public void setAbr(int n) {
        int n2;
        float f;
        this.abr_enabled = n != 0 ? 1 : 0;
        this.vbr_enabled = 1;
        int n3 = n;
        for (n2 = 10; n2 >= 0; --n2) {
            this.setQuality(n2);
            int n4 = this.getBitRate();
            if (n4 <= n3) break;
        }
        if ((f = (float)n2) < 0.0f) {
            f = 0.0f;
        }
        this.setVbrQuality(f);
        this.abr_count = 0.0f;
        this.abr_drift = 0.0f;
        this.abr_drift2 = 0.0f;
    }

    public void setVbrQuality(float f) {
        if (f < 0.0f) {
            f = 0.0f;
        }
        if (f > 10.0f) {
            f = 10.0f;
        }
        this.vbr_quality = f;
    }

    public float getVbrQuality() {
        return this.vbr_quality;
    }

    public void setComplexity(int n) {
        if (n < 0) {
            n = 0;
        }
        if (n > 10) {
            n = 10;
        }
        this.complexity = n;
    }

    public int getComplexity() {
        return this.complexity;
    }

    public void setSamplingRate(int n) {
        this.sampling_rate = n;
    }

    public int getSamplingRate() {
        return this.sampling_rate;
    }

    public int getLookAhead() {
        return this.windowSize - this.frameSize;
    }

    public float getRelativeQuality() {
        return this.relative_quality;
    }
}


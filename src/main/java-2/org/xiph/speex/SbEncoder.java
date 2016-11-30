/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.CbSearch;
import org.xiph.speex.Codebook;
import org.xiph.speex.Encoder;
import org.xiph.speex.Filters;
import org.xiph.speex.Lpc;
import org.xiph.speex.Lsp;
import org.xiph.speex.LspQuant;
import org.xiph.speex.Misc;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.SbCodec;
import org.xiph.speex.SubMode;
import org.xiph.speex.Vbr;

public class SbEncoder
extends SbCodec
implements Encoder {
    public static final int[] NB_QUALITY_MAP = new int[]{1, 8, 2, 3, 4, 5, 5, 6, 6, 7, 7};
    public static final int[] WB_QUALITY_MAP = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4};
    public static final int[] UWB_QUALITY_MAP = new int[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    protected Encoder lowenc;
    private float[] x1d;
    private float[] h0_mem;
    private float[] buf;
    private float[] swBuf;
    private float[] res;
    private float[] target;
    private float[] window;
    private float[] lagWindow;
    private float[] rc;
    private float[] autocorr;
    private float[] lsp;
    private float[] old_lsp;
    private float[] interp_lsp;
    private float[] interp_lpc;
    private float[] bw_lpc1;
    private float[] bw_lpc2;
    private float[] mem_sp2;
    private float[] mem_sw;
    protected int nb_modes;
    private boolean uwb;
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

    public void wbinit() {
        this.lowenc = new NbEncoder();
        ((NbEncoder)this.lowenc).nbinit();
        super.wbinit();
        this.init(160, 40, 8, 640, 0.9f);
        this.uwb = false;
        this.nb_modes = 5;
        this.sampling_rate = 16000;
    }

    public void uwbinit() {
        this.lowenc = new SbEncoder();
        ((SbEncoder)this.lowenc).wbinit();
        super.uwbinit();
        this.init(320, 80, 8, 1280, 0.7f);
        this.uwb = true;
        this.nb_modes = 2;
        this.sampling_rate = 32000;
    }

    public void init(int n, int n2, int n3, int n4, float f) {
        super.init(n, n2, n3, n4, f);
        this.complexity = 3;
        this.vbr_enabled = 0;
        this.vad_enabled = 0;
        this.abr_enabled = 0;
        this.vbr_quality = 8.0f;
        this.submodeSelect = this.submodeID;
        this.x1d = new float[n];
        this.h0_mem = new float[64];
        this.buf = new float[this.windowSize];
        this.swBuf = new float[n];
        this.res = new float[n];
        this.target = new float[n2];
        this.window = Misc.window(this.windowSize, n2);
        this.lagWindow = Misc.lagWindow(n3, this.lag_factor);
        this.rc = new float[n3];
        this.autocorr = new float[n3 + 1];
        this.lsp = new float[n3];
        this.old_lsp = new float[n3];
        this.interp_lsp = new float[n3];
        this.interp_lpc = new float[n3 + 1];
        this.bw_lpc1 = new float[n3 + 1];
        this.bw_lpc2 = new float[n3 + 1];
        this.mem_sp2 = new float[n3];
        this.mem_sw = new float[n3];
        this.abr_count = 0.0f;
    }

    public int encode(Bits bits, float[] arrf) {
        int n;
        int n2;
        int n3;
        float f;
        float f2;
        Filters.qmf_decomp(arrf, Codebook.h0, this.x0d, this.x1d, this.fullFrameSize, 64, this.h0_mem);
        this.lowenc.encode(bits, this.x0d);
        for (n3 = 0; n3 < this.windowSize - this.frameSize; ++n3) {
            this.high[n3] = this.high[this.frameSize + n3];
        }
        for (n3 = 0; n3 < this.frameSize; ++n3) {
            this.high[this.windowSize - this.frameSize + n3] = this.x1d[n3];
        }
        System.arraycopy(this.excBuf, this.frameSize, this.excBuf, 0, this.bufSize - this.frameSize);
        float[] arrf2 = this.lowenc.getPiGain();
        float[] arrf3 = this.lowenc.getExc();
        float[] arrf4 = this.lowenc.getInnov();
        int n4 = this.lowenc.getMode();
        boolean bl = n4 == 0;
        for (n3 = 0; n3 < this.windowSize; ++n3) {
            this.buf[n3] = this.high[n3] * this.window[n3];
        }
        Lpc.autocorr(this.buf, this.autocorr, this.lpcSize + 1, this.windowSize);
        float[] arrf5 = this.autocorr;
        arrf5[0] = arrf5[0] + 1.0f;
        float[] arrf6 = this.autocorr;
        arrf6[0] = arrf6[0] * this.lpc_floor;
        for (n3 = 0; n3 < this.lpcSize + 1; ++n3) {
            float[] arrf7 = this.autocorr;
            int n5 = n3;
            arrf7[n5] = arrf7[n5] * this.lagWindow[n3];
        }
        Lpc.wld(this.lpc, this.autocorr, this.rc, this.lpcSize);
        System.arraycopy(this.lpc, 0, this.lpc, 1, this.lpcSize);
        this.lpc[0] = 1.0f;
        int n6 = Lsp.lpc2lsp(this.lpc, this.lpcSize, this.lsp, 15, 0.2f);
        if (n6 != this.lpcSize && (n6 = Lsp.lpc2lsp(this.lpc, this.lpcSize, this.lsp, 11, 0.02f)) != this.lpcSize) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.lsp[n3] = (float)Math.cos(3.141592653589793 * (double)(n3 + 1) / (double)(this.lpcSize + 1));
            }
        }
        for (n3 = 0; n3 < this.lpcSize; ++n3) {
            this.lsp[n3] = (float)Math.acos(this.lsp[n3]);
        }
        float f3 = 0.0f;
        for (n3 = 0; n3 < this.lpcSize; ++n3) {
            f3 += (this.old_lsp[n3] - this.lsp[n3]) * (this.old_lsp[n3] - this.lsp[n3]);
        }
        if (!(this.vbr_enabled == 0 && this.vad_enabled == 0 || bl)) {
            float f4 = 0.0f;
            f2 = 0.0f;
            if (this.abr_enabled != 0) {
                float f5 = 0.0f;
                if (this.abr_drift2 * this.abr_drift > 0.0f) {
                    f5 = -1.0E-5f * this.abr_drift / (1.0f + this.abr_count);
                    if (f5 > 0.1f) {
                        f5 = 0.1f;
                    }
                    if (f5 < -0.1f) {
                        f5 = -0.1f;
                    }
                }
                this.vbr_quality += f5;
                if (this.vbr_quality > 10.0f) {
                    this.vbr_quality = 10.0f;
                }
                if (this.vbr_quality < 0.0f) {
                    this.vbr_quality = 0.0f;
                }
            }
            for (n3 = 0; n3 < this.frameSize; ++n3) {
                f4 += this.x0d[n3] * this.x0d[n3];
                f2 += this.high[n3] * this.high[n3];
            }
            f = (float)Math.log((1.0f + f2) / (1.0f + f4));
            this.relative_quality = this.lowenc.getRelativeQuality();
            if (f < -4.0f) {
                f = -4.0f;
            }
            if (f > 2.0f) {
                f = 2.0f;
            }
            if (this.vbr_enabled != 0) {
                float f6;
                n = this.nb_modes - 1;
                this.relative_quality = (float)((double)this.relative_quality + 1.0 * (double)(f + 2.0f));
                if (this.relative_quality < -1.0f) {
                    this.relative_quality = -1.0f;
                }
                while (n != 0 && this.relative_quality < (f6 = (n2 = (int)Math.floor(this.vbr_quality)) == 10 ? Vbr.hb_thresh[n][n2] : (this.vbr_quality - (float)n2) * Vbr.hb_thresh[n][n2 + 1] + ((float)(1 + n2) - this.vbr_quality) * Vbr.hb_thresh[n][n2])) {
                    --n;
                }
                this.setMode(n);
                if (this.abr_enabled != 0) {
                    n2 = this.getBitRate();
                    this.abr_drift += (float)(n2 - this.abr_enabled);
                    this.abr_drift2 = 0.95f * this.abr_drift2 + 0.05f * (float)(n2 - this.abr_enabled);
                    this.abr_count = (float)((double)this.abr_count + 1.0);
                }
            } else {
                n = (double)this.relative_quality < 2.0 ? 1 : this.submodeSelect;
                this.submodeID = n;
            }
        }
        bits.pack(1, 1);
        if (bl) {
            bits.pack(0, 3);
        } else {
            bits.pack(this.submodeID, 3);
        }
        if (bl || this.submodes[this.submodeID] == null) {
            for (n3 = 0; n3 < this.frameSize; ++n3) {
                this.swBuf[n3] = 0.0f;
                this.excBuf[this.excIdx + n3] = 0.0f;
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.mem_sw[n3] = 0.0f;
            }
            this.first = 1;
            Filters.iir_mem2(this.excBuf, this.excIdx, this.interp_qlpc, this.high, 0, this.subframeSize, this.lpcSize, this.mem_sp);
            this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
            this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);
            for (n3 = 0; n3 < this.fullFrameSize; ++n3) {
                arrf[n3] = 2.0f * (this.y0[n3] - this.y1[n3]);
            }
            if (bl) {
                return 0;
            }
            return 1;
        }
        this.submodes[this.submodeID].lsqQuant.quant(this.lsp, this.qlsp, this.lpcSize, bits);
        if (this.first != 0) {
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.old_lsp[n3] = this.lsp[n3];
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.old_qlsp[n3] = this.qlsp[n3];
            }
        }
        float[] arrf8 = new float[this.lpcSize];
        float[] arrf9 = new float[this.subframeSize];
        float[] arrf10 = new float[this.subframeSize];
        for (int i = 0; i < this.nbSubframes; ++i) {
            float f7;
            int n7;
            float f8 = 0.0f;
            float f9 = 0.0f;
            n2 = n7 = this.subframeSize * i;
            n = this.excIdx + n7;
            int n8 = n7;
            int n9 = n7;
            f2 = (1.0f + (float)i) / (float)this.nbSubframes;
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_lsp[n3] = (1.0f - f2) * this.old_lsp[n3] + f2 * this.lsp[n3];
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_qlsp[n3] = (1.0f - f2) * this.old_qlsp[n3] + f2 * this.qlsp[n3];
            }
            Lsp.enforce_margin(this.interp_lsp, this.lpcSize, 0.05f);
            Lsp.enforce_margin(this.interp_qlsp, this.lpcSize, 0.05f);
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_lsp[n3] = (float)Math.cos(this.interp_lsp[n3]);
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                this.interp_qlsp[n3] = (float)Math.cos(this.interp_qlsp[n3]);
            }
            this.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, this.lpcSize);
            this.m_lsp.lsp2lpc(this.interp_qlsp, this.interp_qlpc, this.lpcSize);
            Filters.bw_lpc(this.gamma1, this.interp_lpc, this.bw_lpc1, this.lpcSize);
            Filters.bw_lpc(this.gamma2, this.interp_lpc, this.bw_lpc2, this.lpcSize);
            float f10 = 0.0f;
            float f11 = 0.0f;
            f2 = 1.0f;
            this.pi_gain[i] = 0.0f;
            for (n3 = 0; n3 <= this.lpcSize; ++n3) {
                f10 += f2 * this.interp_qlpc[n3];
                f2 = - f2;
                float[] arrf11 = this.pi_gain;
                int n10 = i;
                arrf11[n10] = arrf11[n10] + this.interp_qlpc[n3];
            }
            f11 = arrf2[i];
            f11 = 1.0f / (Math.abs(f11) + 0.01f);
            f = Math.abs(0.01f + (f10 = 1.0f / (Math.abs(f10) + 0.01f))) / (0.01f + Math.abs(f11));
            boolean bl2 = f < 5.0f;
            bl2 = false;
            Filters.fir_mem2(this.high, n2, this.interp_qlpc, this.excBuf, n, this.subframeSize, this.lpcSize, this.mem_sp2);
            for (n3 = 0; n3 < this.subframeSize; ++n3) {
                f8 += this.excBuf[n + n3] * this.excBuf[n + n3];
            }
            if (this.submodes[this.submodeID].innovation == null) {
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    f9 += arrf4[n7 + n3] * arrf4[n7 + n3];
                }
                f7 = f8 / (0.01f + f9);
                f7 = (float)Math.sqrt(f7);
                int n11 = (int)Math.floor(10.5 + 8.0 * Math.log((double)(f7 *= f) + 1.0E-4));
                if (n11 < 0) {
                    n11 = 0;
                }
                if (n11 > 31) {
                    n11 = 31;
                }
                bits.pack(n11, 5);
                f7 = (float)(0.1 * Math.exp((double)n11 / 9.4));
                f7 /= f;
            } else {
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    f9 += arrf3[n7 + n3] * arrf3[n7 + n3];
                }
                f7 = (float)(Math.sqrt(1.0f + f8) * (double)f / Math.sqrt((1.0f + f9) * (float)this.subframeSize));
                int n12 = (int)Math.floor(0.5 + 3.7 * (Math.log(f7) + 2.0));
                if (n12 < 0) {
                    n12 = 0;
                }
                if (n12 > 15) {
                    n12 = 15;
                }
                bits.pack(n12, 4);
                f7 = (float)Math.exp(0.27027027027027023 * (double)n12 - 2.0);
                float f12 = f7 * (float)Math.sqrt(1.0f + f9) / f;
                float f13 = 1.0f / f12;
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    this.excBuf[n + n3] = 0.0f;
                }
                this.excBuf[n] = 1.0f;
                Filters.syn_percep_zero(this.excBuf, n, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, arrf9, this.subframeSize, this.lpcSize);
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    this.excBuf[n + n3] = 0.0f;
                }
                for (n3 = 0; n3 < this.lpcSize; ++n3) {
                    arrf8[n3] = this.mem_sp[n3];
                }
                Filters.iir_mem2(this.excBuf, n, this.interp_qlpc, this.excBuf, n, this.subframeSize, this.lpcSize, arrf8);
                for (n3 = 0; n3 < this.lpcSize; ++n3) {
                    arrf8[n3] = this.mem_sw[n3];
                }
                Filters.filter_mem2(this.excBuf, n, this.bw_lpc1, this.bw_lpc2, this.res, n8, this.subframeSize, this.lpcSize, arrf8, 0);
                for (n3 = 0; n3 < this.lpcSize; ++n3) {
                    arrf8[n3] = this.mem_sw[n3];
                }
                Filters.filter_mem2(this.high, n2, this.bw_lpc1, this.bw_lpc2, this.swBuf, n9, this.subframeSize, this.lpcSize, arrf8, 0);
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    this.target[n3] = this.swBuf[n9 + n3] - this.res[n8 + n3];
                }
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    this.excBuf[n + n3] = 0.0f;
                }
                n3 = 0;
                while (n3 < this.subframeSize) {
                    float[] arrf12 = this.target;
                    int n13 = n3++;
                    arrf12[n13] = arrf12[n13] * f13;
                }
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    arrf10[n3] = 0.0f;
                }
                this.submodes[this.submodeID].innovation.quant(this.target, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.lpcSize, this.subframeSize, arrf10, 0, arrf9, bits, this.complexity + 1 >> 1);
                for (n3 = 0; n3 < this.subframeSize; ++n3) {
                    float[] arrf13 = this.excBuf;
                    int n14 = n + n3;
                    arrf13[n14] = arrf13[n14] + arrf10[n3] * f12;
                }
                if (this.submodes[this.submodeID].double_codebook != 0) {
                    float[] arrf14 = new float[this.subframeSize];
                    for (n3 = 0; n3 < this.subframeSize; ++n3) {
                        arrf14[n3] = 0.0f;
                    }
                    n3 = 0;
                    while (n3 < this.subframeSize) {
                        float[] arrf15 = this.target;
                        int n15 = n3++;
                        arrf15[n15] = (float)((double)arrf15[n15] * 2.5);
                    }
                    this.submodes[this.submodeID].innovation.quant(this.target, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.lpcSize, this.subframeSize, arrf14, 0, arrf9, bits, this.complexity + 1 >> 1);
                    n3 = 0;
                    while (n3 < this.subframeSize) {
                        float[] arrf16 = arrf14;
                        int n16 = n3++;
                        arrf16[n16] = (float)((double)arrf16[n16] * ((double)f12 * 0.4));
                    }
                    for (n3 = 0; n3 < this.subframeSize; ++n3) {
                        float[] arrf17 = this.excBuf;
                        int n17 = n + n3;
                        arrf17[n17] = arrf17[n17] + arrf14[n3];
                    }
                }
            }
            for (n3 = 0; n3 < this.lpcSize; ++n3) {
                arrf8[n3] = this.mem_sp[n3];
            }
            Filters.iir_mem2(this.excBuf, n, this.interp_qlpc, this.high, n2, this.subframeSize, this.lpcSize, this.mem_sp);
            Filters.filter_mem2(this.high, n2, this.bw_lpc1, this.bw_lpc2, this.swBuf, n9, this.subframeSize, this.lpcSize, this.mem_sw, 0);
        }
        this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
        this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);
        for (n3 = 0; n3 < this.fullFrameSize; ++n3) {
            arrf[n3] = 2.0f * (this.y0[n3] - this.y1[n3]);
        }
        for (n3 = 0; n3 < this.lpcSize; ++n3) {
            this.old_lsp[n3] = this.lsp[n3];
        }
        for (n3 = 0; n3 < this.lpcSize; ++n3) {
            this.old_qlsp[n3] = this.qlsp[n3];
        }
        this.first = 0;
        return 1;
    }

    public int getEncodedFrameSize() {
        int n = SB_FRAME_SIZE[this.submodeID];
        return n += this.lowenc.getEncodedFrameSize();
    }

    public void setQuality(int n) {
        if (n < 0) {
            n = 0;
        }
        if (n > 10) {
            n = 10;
        }
        if (this.uwb) {
            this.lowenc.setQuality(n);
            this.setMode(UWB_QUALITY_MAP[n]);
        } else {
            this.lowenc.setMode(NB_QUALITY_MAP[n]);
            this.setMode(WB_QUALITY_MAP[n]);
        }
    }

    public void setVbrQuality(float f) {
        this.vbr_quality = f;
        float f2 = f + 0.6f;
        if (f2 > 10.0f) {
            f2 = 10.0f;
        }
        this.lowenc.setVbrQuality(f2);
        int n = (int)Math.floor(0.5 + (double)f);
        if (n > 10) {
            n = 10;
        }
        this.setQuality(n);
    }

    public void setVbr(boolean bl) {
        this.vbr_enabled = bl ? 1 : 0;
        this.lowenc.setVbr(bl);
    }

    public void setAbr(int n) {
        int n2;
        float f;
        this.lowenc.setVbr(true);
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

    public int getBitRate() {
        if (this.submodes[this.submodeID] != null) {
            return this.lowenc.getBitRate() + this.sampling_rate * this.submodes[this.submodeID].bits_per_frame / this.frameSize;
        }
        return this.lowenc.getBitRate() + this.sampling_rate * 4 / this.frameSize;
    }

    public void setSamplingRate(int n) {
        this.sampling_rate = n;
        this.lowenc.setSamplingRate(n);
    }

    public int getLookAhead() {
        return 2 * this.lowenc.getLookAhead() + 64 - 1;
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

    public int getSamplingRate() {
        return this.sampling_rate;
    }

    public float getRelativeQuality() {
        return this.relative_quality;
    }
}


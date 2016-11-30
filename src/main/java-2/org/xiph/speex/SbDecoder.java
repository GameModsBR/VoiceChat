/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.StreamCorruptedException;
import org.xiph.speex.Bits;
import org.xiph.speex.CbSearch;
import org.xiph.speex.Codebook;
import org.xiph.speex.Decoder;
import org.xiph.speex.Filters;
import org.xiph.speex.Lsp;
import org.xiph.speex.LspQuant;
import org.xiph.speex.NbDecoder;
import org.xiph.speex.SbCodec;
import org.xiph.speex.Stereo;
import org.xiph.speex.SubMode;

public class SbDecoder
extends SbCodec
implements Decoder {
    protected Decoder lowdec;
    protected Stereo stereo = new Stereo();
    protected boolean enhanced = true;
    private float[] innov2;

    public void wbinit() {
        this.lowdec = new NbDecoder();
        ((NbDecoder)this.lowdec).nbinit();
        this.lowdec.setPerceptualEnhancement(this.enhanced);
        super.wbinit();
        this.init(160, 40, 8, 640, 0.7f);
    }

    public void uwbinit() {
        this.lowdec = new SbDecoder();
        ((SbDecoder)this.lowdec).wbinit();
        this.lowdec.setPerceptualEnhancement(this.enhanced);
        super.uwbinit();
        this.init(320, 80, 8, 1280, 0.5f);
    }

    public void init(int n, int n2, int n3, int n4, float f) {
        super.init(n, n2, n3, n4, f);
        this.excIdx = 0;
        this.innov2 = new float[n2];
    }

    public int decode(Bits bits, float[] arrf) throws StreamCorruptedException {
        int n;
        int n2 = this.lowdec.decode(bits, this.x0d);
        if (n2 != 0) {
            return n2;
        }
        boolean bl = this.lowdec.getDtx();
        if (bits == null) {
            this.decodeLost(arrf, bl);
            return 0;
        }
        int n3 = bits.peek();
        if (n3 != 0) {
            n3 = bits.unpack(1);
            this.submodeID = bits.unpack(3);
        } else {
            this.submodeID = 0;
        }
        for (n = 0; n < this.frameSize; ++n) {
            this.excBuf[n] = 0.0f;
        }
        if (this.submodes[this.submodeID] == null) {
            if (bl) {
                this.decodeLost(arrf, true);
                return 0;
            }
            for (n = 0; n < this.frameSize; ++n) {
                this.excBuf[n] = 0.0f;
            }
            this.first = 1;
            Filters.iir_mem2(this.excBuf, this.excIdx, this.interp_qlpc, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp);
            this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
            this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);
            for (n = 0; n < this.fullFrameSize; ++n) {
                arrf[n] = 2.0f * (this.y0[n] - this.y1[n]);
            }
            return 0;
        }
        float[] arrf2 = this.lowdec.getPiGain();
        float[] arrf3 = this.lowdec.getExc();
        float[] arrf4 = this.lowdec.getInnov();
        this.submodes[this.submodeID].lsqQuant.unquant(this.qlsp, this.lpcSize, bits);
        if (this.first != 0) {
            for (n = 0; n < this.lpcSize; ++n) {
                this.old_qlsp[n] = this.qlsp[n];
            }
        }
        for (int i = 0; i < this.nbSubframes; ++i) {
            float f;
            float f2;
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = 0.0f;
            int n4 = this.subframeSize * i;
            float f6 = (1.0f + (float)i) / (float)this.nbSubframes;
            for (n = 0; n < this.lpcSize; ++n) {
                this.interp_qlsp[n] = (1.0f - f6) * this.old_qlsp[n] + f6 * this.qlsp[n];
            }
            Lsp.enforce_margin(this.interp_qlsp, this.lpcSize, 0.05f);
            for (n = 0; n < this.lpcSize; ++n) {
                this.interp_qlsp[n] = (float)Math.cos(this.interp_qlsp[n]);
            }
            this.m_lsp.lsp2lpc(this.interp_qlsp, this.interp_qlpc, this.lpcSize);
            if (this.enhanced) {
                f2 = this.submodes[this.submodeID].lpc_enh_k1;
                f = this.submodes[this.submodeID].lpc_enh_k2;
                float f7 = f2 - f;
                Filters.bw_lpc(f2, this.interp_qlpc, this.awk1, this.lpcSize);
                Filters.bw_lpc(f, this.interp_qlpc, this.awk2, this.lpcSize);
                Filters.bw_lpc(f7, this.interp_qlpc, this.awk3, this.lpcSize);
            }
            f6 = 1.0f;
            this.pi_gain[i] = 0.0f;
            for (n = 0; n <= this.lpcSize; ++n) {
                f5 += f6 * this.interp_qlpc[n];
                f6 = - f6;
                float[] arrf5 = this.pi_gain;
                int n5 = i;
                arrf5[n5] = arrf5[n5] + this.interp_qlpc[n];
            }
            f4 = arrf2[i];
            f4 = 1.0f / (Math.abs(f4) + 0.01f);
            f5 = 1.0f / (Math.abs(f5) + 0.01f);
            float f8 = Math.abs(0.01f + f5) / (0.01f + Math.abs(f4));
            for (n = n4; n < n4 + this.subframeSize; ++n) {
                this.excBuf[n] = 0.0f;
            }
            if (this.submodes[this.submodeID].innovation == null) {
                int n6 = bits.unpack(5);
                f2 = (float)Math.exp(((double)n6 - 10.0) / 8.0);
                f2 /= f8;
                for (n = n4; n < n4 + this.subframeSize; ++n) {
                    this.excBuf[n] = this.foldingGain * f2 * arrf4[n];
                }
            } else {
                int n7 = bits.unpack(4);
                for (n = n4; n < n4 + this.subframeSize; ++n) {
                    f3 += arrf3[n] * arrf3[n];
                }
                f2 = (float)Math.exp(0.27027026f * (float)n7 - 2.0f);
                f = f2 * (float)Math.sqrt(1.0f + f3) / f8;
                this.submodes[this.submodeID].innovation.unquant(this.excBuf, n4, this.subframeSize, bits);
                n = n4;
                while (n < n4 + this.subframeSize) {
                    float[] arrf6 = this.excBuf;
                    int n8 = n++;
                    arrf6[n8] = arrf6[n8] * f;
                }
                if (this.submodes[this.submodeID].double_codebook != 0) {
                    for (n = 0; n < this.subframeSize; ++n) {
                        this.innov2[n] = 0.0f;
                    }
                    this.submodes[this.submodeID].innovation.unquant(this.innov2, 0, this.subframeSize, bits);
                    n = 0;
                    while (n < this.subframeSize) {
                        float[] arrf7 = this.innov2;
                        int n9 = n++;
                        arrf7[n9] = arrf7[n9] * (f * 0.4f);
                    }
                    for (n = 0; n < this.subframeSize; ++n) {
                        float[] arrf8 = this.excBuf;
                        int n10 = n4 + n;
                        arrf8[n10] = arrf8[n10] + this.innov2[n];
                    }
                }
            }
            for (n = n4; n < n4 + this.subframeSize; ++n) {
                this.high[n] = this.excBuf[n];
            }
            if (this.enhanced) {
                Filters.filter_mem2(this.high, n4, this.awk2, this.awk1, this.subframeSize, this.lpcSize, this.mem_sp, this.lpcSize);
                Filters.filter_mem2(this.high, n4, this.awk3, this.interp_qlpc, this.subframeSize, this.lpcSize, this.mem_sp, 0);
                continue;
            }
            for (n = 0; n < this.lpcSize; ++n) {
                this.mem_sp[this.lpcSize + n] = 0.0f;
            }
            Filters.iir_mem2(this.high, n4, this.interp_qlpc, this.high, n4, this.subframeSize, this.lpcSize, this.mem_sp);
        }
        this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
        this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);
        for (n = 0; n < this.fullFrameSize; ++n) {
            arrf[n] = 2.0f * (this.y0[n] - this.y1[n]);
        }
        for (n = 0; n < this.lpcSize; ++n) {
            this.old_qlsp[n] = this.qlsp[n];
        }
        this.first = 0;
        return 0;
    }

    public int decodeLost(float[] arrf, boolean bl) {
        int n;
        int n2 = 0;
        if (bl) {
            n2 = this.submodeID;
            this.submodeID = 1;
        } else {
            Filters.bw_lpc(0.99f, this.interp_qlpc, this.interp_qlpc, this.lpcSize);
        }
        this.first = 1;
        this.awk1 = new float[this.lpcSize + 1];
        this.awk2 = new float[this.lpcSize + 1];
        this.awk3 = new float[this.lpcSize + 1];
        if (this.enhanced) {
            float f;
            float f2;
            if (this.submodes[this.submodeID] != null) {
                f = this.submodes[this.submodeID].lpc_enh_k1;
                f2 = this.submodes[this.submodeID].lpc_enh_k2;
            } else {
                f2 = 0.7f;
                f = 0.7f;
            }
            float f3 = f - f2;
            Filters.bw_lpc(f, this.interp_qlpc, this.awk1, this.lpcSize);
            Filters.bw_lpc(f2, this.interp_qlpc, this.awk2, this.lpcSize);
            Filters.bw_lpc(f3, this.interp_qlpc, this.awk3, this.lpcSize);
        }
        if (!bl) {
            for (n = 0; n < this.frameSize; ++n) {
                float[] arrf2 = this.excBuf;
                int n3 = this.excIdx + n;
                arrf2[n3] = (float)((double)arrf2[n3] * 0.9);
            }
        }
        for (n = 0; n < this.frameSize; ++n) {
            this.high[n] = this.excBuf[this.excIdx + n];
        }
        if (this.enhanced) {
            Filters.filter_mem2(this.high, 0, this.awk2, this.awk1, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp, this.lpcSize);
            Filters.filter_mem2(this.high, 0, this.awk3, this.interp_qlpc, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp, 0);
        } else {
            for (n = 0; n < this.lpcSize; ++n) {
                this.mem_sp[this.lpcSize + n] = 0.0f;
            }
            Filters.iir_mem2(this.high, 0, this.interp_qlpc, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp);
        }
        this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
        this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);
        for (n = 0; n < this.fullFrameSize; ++n) {
            arrf[n] = 2.0f * (this.y0[n] - this.y1[n]);
        }
        if (bl) {
            this.submodeID = n2;
        }
        return 0;
    }

    public void decodeStereo(float[] arrf, int n) {
        this.stereo.decode(arrf, n);
    }

    public void setPerceptualEnhancement(boolean bl) {
        this.enhanced = bl;
    }

    public boolean getPerceptualEnhancement() {
        return this.enhanced;
    }
}


/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.StreamCorruptedException;
import java.util.Random;
import org.xiph.speex.Bits;
import org.xiph.speex.CbSearch;
import org.xiph.speex.Decoder;
import org.xiph.speex.Filters;
import org.xiph.speex.Inband;
import org.xiph.speex.Lsp;
import org.xiph.speex.LspQuant;
import org.xiph.speex.Ltp;
import org.xiph.speex.NbCodec;
import org.xiph.speex.SbCodec;
import org.xiph.speex.Stereo;
import org.xiph.speex.SubMode;

public class NbDecoder
extends NbCodec
implements Decoder {
    private float[] innov2;
    private int count_lost;
    private int last_pitch;
    private float last_pitch_gain;
    private float[] pitch_gain_buf;
    private int pitch_gain_buf_idx;
    private float last_ol_gain;
    protected Random random = new Random();
    protected Stereo stereo = new Stereo();
    protected Inband inband = new Inband(this.stereo);
    protected boolean enhanced = true;

    public void init(int n, int n2, int n3, int n4) {
        super.init(n, n2, n3, n4);
        this.filters.init();
        this.innov2 = new float[40];
        this.count_lost = 0;
        this.last_pitch = 40;
        this.last_pitch_gain = 0.0f;
        this.pitch_gain_buf = new float[3];
        this.pitch_gain_buf_idx = 0;
        this.last_ol_gain = 0.0f;
    }

    public int decode(Bits bits, float[] arrf) throws StreamCorruptedException {
        int n;
        int n2;
        int n3 = 0;
        float[] arrf2 = new float[3];
        float f = 0.0f;
        float f2 = 0.0f;
        int n4 = 40;
        float f3 = 0.0f;
        float f4 = 0.0f;
        if (bits == null && this.dtx_enabled != 0) {
            this.submodeID = 0;
        } else {
            int n5;
            if (bits == null) {
                this.decodeLost(arrf);
                return 0;
            }
            do {
                if (bits.unpack(1) != 0) {
                    n5 = bits.unpack(3);
                    int n6 = SbCodec.SB_FRAME_SIZE[n5];
                    if (n6 < 0) {
                        throw new StreamCorruptedException("Invalid sideband mode encountered (1st sideband): " + n5);
                    }
                    bits.advance(n6 -= 4);
                    if (bits.unpack(1) != 0) {
                        n5 = bits.unpack(3);
                        n6 = SbCodec.SB_FRAME_SIZE[n5];
                        if (n6 < 0) {
                            throw new StreamCorruptedException("Invalid sideband mode encountered. (2nd sideband): " + n5);
                        }
                        bits.advance(n6 -= 4);
                        if (bits.unpack(1) != 0) {
                            throw new StreamCorruptedException("More than two sideband layers found");
                        }
                    }
                }
                if ((n5 = bits.unpack(4)) == 15) {
                    return 1;
                }
                if (n5 == 14) {
                    this.inband.speexInbandRequest(bits);
                    continue;
                }
                if (n5 == 13) {
                    this.inband.userInbandRequest(bits);
                    continue;
                }
                if (n5 <= 8) continue;
                throw new StreamCorruptedException("Invalid mode encountered: " + n5);
            } while (n5 > 8);
            this.submodeID = n5;
        }
        System.arraycopy(this.frmBuf, this.frameSize, this.frmBuf, 0, this.bufSize - this.frameSize);
        System.arraycopy(this.excBuf, this.frameSize, this.excBuf, 0, this.bufSize - this.frameSize);
        if (this.submodes[this.submodeID] == null) {
            int n7;
            Filters.bw_lpc(0.93f, this.interp_qlpc, this.lpc, 10);
            float f5 = 0.0f;
            for (n7 = 0; n7 < this.frameSize; ++n7) {
                f5 += this.innov[n7] * this.innov[n7];
            }
            f5 = (float)Math.sqrt(f5 / (float)this.frameSize);
            for (n7 = this.excIdx; n7 < this.excIdx + this.frameSize; ++n7) {
                this.excBuf[n7] = 3.0f * f5 * (this.random.nextFloat() - 0.5f);
            }
            this.first = 1;
            Filters.iir_mem2(this.excBuf, this.excIdx, this.lpc, this.frmBuf, this.frmIdx, this.frameSize, this.lpcSize, this.mem_sp);
            arrf[0] = this.frmBuf[this.frmIdx] + this.preemph * this.pre_mem;
            for (n7 = 1; n7 < this.frameSize; ++n7) {
                arrf[n7] = this.frmBuf[this.frmIdx + n7] + this.preemph * arrf[n7 - 1];
            }
            this.pre_mem = arrf[this.frameSize - 1];
            this.count_lost = 0;
            return 0;
        }
        this.submodes[this.submodeID].lsqQuant.unquant(this.qlsp, this.lpcSize, bits);
        if (this.count_lost != 0) {
            float f6 = 0.0f;
            for (n2 = 0; n2 < this.lpcSize; ++n2) {
                f6 += Math.abs(this.old_qlsp[n2] - this.qlsp[n2]);
            }
            float f7 = (float)(0.6 * Math.exp(-0.2 * (double)f6));
            n2 = 0;
            while (n2 < 2 * this.lpcSize) {
                float[] arrf3 = this.mem_sp;
                int n8 = n2++;
                arrf3[n8] = arrf3[n8] * f7;
            }
        }
        if (this.first != 0 || this.count_lost != 0) {
            for (n2 = 0; n2 < this.lpcSize; ++n2) {
                this.old_qlsp[n2] = this.qlsp[n2];
            }
        }
        if (this.submodes[this.submodeID].lbr_pitch != -1) {
            n3 = this.min_pitch + bits.unpack(7);
        }
        if (this.submodes[this.submodeID].forced_pitch_gain != 0) {
            n = bits.unpack(4);
            f2 = 0.066667f * (float)n;
        }
        n = bits.unpack(5);
        f = (float)Math.exp((double)n / 3.5);
        if (this.submodeID == 1) {
            int n9 = bits.unpack(4);
            this.dtx_enabled = n9 == 15 ? 1 : 0;
        }
        if (this.submodeID > 1) {
            this.dtx_enabled = 0;
        }
        for (int i = 0; i < this.nbSubframes; ++i) {
            float f8;
            int n10;
            int n11;
            float f9;
            int n12;
            int n13 = this.subframeSize * i;
            int n14 = this.frmIdx + n13;
            int n15 = this.excIdx + n13;
            float f10 = (1.0f + (float)i) / (float)this.nbSubframes;
            for (n2 = 0; n2 < this.lpcSize; ++n2) {
                this.interp_qlsp[n2] = (1.0f - f10) * this.old_qlsp[n2] + f10 * this.qlsp[n2];
            }
            Lsp.enforce_margin(this.interp_qlsp, this.lpcSize, 0.002f);
            for (n2 = 0; n2 < this.lpcSize; ++n2) {
                this.interp_qlsp[n2] = (float)Math.cos(this.interp_qlsp[n2]);
            }
            this.m_lsp.lsp2lpc(this.interp_qlsp, this.interp_qlpc, this.lpcSize);
            if (this.enhanced) {
                float f11 = 0.9f;
                float f12 = this.submodes[this.submodeID].lpc_enh_k1;
                f8 = this.submodes[this.submodeID].lpc_enh_k2;
                float f13 = (1.0f - (1.0f - f11 * f12) / (1.0f - f11 * f8)) / f11;
                Filters.bw_lpc(f12, this.interp_qlpc, this.awk1, this.lpcSize);
                Filters.bw_lpc(f8, this.interp_qlpc, this.awk2, this.lpcSize);
                Filters.bw_lpc(f13, this.interp_qlpc, this.awk3, this.lpcSize);
            }
            f10 = 1.0f;
            this.pi_gain[i] = 0.0f;
            for (n2 = 0; n2 <= this.lpcSize; ++n2) {
                float[] arrf4 = this.pi_gain;
                int n16 = i;
                arrf4[n16] = arrf4[n16] + f10 * this.interp_qlpc[n2];
                f10 = - f10;
            }
            for (n2 = 0; n2 < this.subframeSize; ++n2) {
                this.excBuf[n15 + n2] = 0.0f;
            }
            if (this.submodes[this.submodeID].lbr_pitch != -1) {
                int n17 = this.submodes[this.submodeID].lbr_pitch;
                if (n17 != 0) {
                    n11 = n3 - n17 + 1;
                    if (n11 < this.min_pitch) {
                        n11 = this.min_pitch;
                    }
                    if ((n10 = n3 + n17) > this.max_pitch) {
                        n10 = this.max_pitch;
                    }
                } else {
                    n11 = n10 = n3;
                }
            } else {
                n11 = this.min_pitch;
                n10 = this.max_pitch;
            }
            int n18 = this.submodes[this.submodeID].ltp.unquant(this.excBuf, n15, n11, f2, this.subframeSize, arrf2, bits, this.count_lost, n13, this.last_pitch_gain);
            if (this.count_lost != 0 && f < this.last_ol_gain) {
                float f14 = f / (this.last_ol_gain + 1.0f);
                for (n2 = 0; n2 < this.subframeSize; ++n2) {
                    float[] arrf5 = this.excBuf;
                    int n19 = this.excIdx + n2;
                    arrf5[n19] = arrf5[n19] * f14;
                }
            }
            f10 = Math.abs(arrf2[0] + arrf2[1] + arrf2[2]);
            f10 = Math.abs(arrf2[1]);
            f10 = arrf2[0] > 0.0f ? (f10 += arrf2[0]) : (float)((double)f10 - 0.5 * (double)arrf2[0]);
            f10 = arrf2[2] > 0.0f ? (f10 += arrf2[2]) : (float)((double)f10 - 0.5 * (double)arrf2[0]);
            f4 += f10;
            if (f10 > f3) {
                n4 = n18;
                f3 = f10;
            }
            for (n2 = n12 = i * this.subframeSize; n2 < n12 + this.subframeSize; ++n2) {
                this.innov[n2] = 0.0f;
            }
            if (this.submodes[this.submodeID].have_subframe_gain == 3) {
                f8 = bits.unpack(3);
                f9 = (float)((double)f * Math.exp(exc_gain_quant_scal3[f8]));
            } else if (this.submodes[this.submodeID].have_subframe_gain == 1) {
                f8 = bits.unpack(1);
                f9 = (float)((double)f * Math.exp(exc_gain_quant_scal1[f8]));
            } else {
                f9 = f;
            }
            if (this.submodes[this.submodeID].innovation != null) {
                this.submodes[this.submodeID].innovation.unquant(this.innov, n12, this.subframeSize, bits);
            }
            n2 = n12;
            while (n2 < n12 + this.subframeSize) {
                float[] arrf6 = this.innov;
                int n20 = n2++;
                arrf6[n20] = arrf6[n20] * f9;
            }
            if (this.submodeID == 1) {
                float f15 = f2;
                for (n2 = 0; n2 < this.subframeSize; ++n2) {
                    this.excBuf[n15 + n2] = 0.0f;
                }
                while (this.voc_offset < this.subframeSize) {
                    if (this.voc_offset >= 0) {
                        this.excBuf[n15 + this.voc_offset] = (float)Math.sqrt(1.0f * (float)n3);
                    }
                    this.voc_offset += n3;
                }
                this.voc_offset -= this.subframeSize;
                if ((f15 = 0.5f + 2.0f * (f15 - 0.6f)) < 0.0f) {
                    f15 = 0.0f;
                }
                if (f15 > 1.0f) {
                    f15 = 1.0f;
                }
                for (n2 = 0; n2 < this.subframeSize; ++n2) {
                    float f16 = this.excBuf[n15 + n2];
                    this.excBuf[n15 + n2] = 0.8f * f15 * this.excBuf[n15 + n2] * f + 0.6f * f15 * this.voc_m1 * f + 0.5f * f15 * this.innov[n12 + n2] - 0.5f * f15 * this.voc_m2 + (1.0f - f15) * this.innov[n12 + n2];
                    this.voc_m1 = f16;
                    this.voc_m2 = this.innov[n12 + n2];
                    this.voc_mean = 0.95f * this.voc_mean + 0.05f * this.excBuf[n15 + n2];
                    float[] arrf7 = this.excBuf;
                    int n21 = n15 + n2;
                    arrf7[n21] = arrf7[n21] - this.voc_mean;
                }
            } else {
                for (n2 = 0; n2 < this.subframeSize; ++n2) {
                    float[] arrf8 = this.excBuf;
                    int n22 = n15 + n2;
                    arrf8[n22] = arrf8[n22] + this.innov[n12 + n2];
                }
            }
            if (this.submodes[this.submodeID].double_codebook != 0) {
                for (n2 = 0; n2 < this.subframeSize; ++n2) {
                    this.innov2[n2] = 0.0f;
                }
                this.submodes[this.submodeID].innovation.unquant(this.innov2, 0, this.subframeSize, bits);
                n2 = 0;
                while (n2 < this.subframeSize) {
                    float[] arrf9 = this.innov2;
                    int n23 = n2++;
                    arrf9[n23] = (float)((double)arrf9[n23] * ((double)f9 * 0.45454545454545453));
                }
                for (n2 = 0; n2 < this.subframeSize; ++n2) {
                    float[] arrf10 = this.excBuf;
                    int n24 = n15 + n2;
                    arrf10[n24] = arrf10[n24] + this.innov2[n2];
                }
            }
            for (n2 = 0; n2 < this.subframeSize; ++n2) {
                this.frmBuf[n14 + n2] = this.excBuf[n15 + n2];
            }
            if (this.enhanced && this.submodes[this.submodeID].comb_gain > 0.0f) {
                this.filters.comb_filter(this.excBuf, n15, this.frmBuf, n14, this.subframeSize, n18, arrf2, this.submodes[this.submodeID].comb_gain);
            }
            if (this.enhanced) {
                Filters.filter_mem2(this.frmBuf, n14, this.awk2, this.awk1, this.subframeSize, this.lpcSize, this.mem_sp, this.lpcSize);
                Filters.filter_mem2(this.frmBuf, n14, this.awk3, this.interp_qlpc, this.subframeSize, this.lpcSize, this.mem_sp, 0);
                continue;
            }
            for (n2 = 0; n2 < this.lpcSize; ++n2) {
                this.mem_sp[this.lpcSize + n2] = 0.0f;
            }
            Filters.iir_mem2(this.frmBuf, n14, this.interp_qlpc, this.frmBuf, n14, this.subframeSize, this.lpcSize, this.mem_sp);
        }
        arrf[0] = this.frmBuf[this.frmIdx] + this.preemph * this.pre_mem;
        for (n2 = 1; n2 < this.frameSize; ++n2) {
            arrf[n2] = this.frmBuf[this.frmIdx + n2] + this.preemph * arrf[n2 - 1];
        }
        this.pre_mem = arrf[this.frameSize - 1];
        for (n2 = 0; n2 < this.lpcSize; ++n2) {
            this.old_qlsp[n2] = this.qlsp[n2];
        }
        this.first = 0;
        this.count_lost = 0;
        this.last_pitch = n4;
        this.last_pitch_gain = 0.25f * f4;
        this.pitch_gain_buf[this.pitch_gain_buf_idx++] = this.last_pitch_gain;
        if (this.pitch_gain_buf_idx > 2) {
            this.pitch_gain_buf_idx = 0;
        }
        this.last_ol_gain = f;
        return 0;
    }

    public int decodeLost(float[] arrf) {
        float f;
        int n;
        float f2;
        float f3 = (float)Math.exp(-0.04 * (double)this.count_lost * (double)this.count_lost);
        float f4 = this.pitch_gain_buf[0] < this.pitch_gain_buf[1] ? (this.pitch_gain_buf[1] < this.pitch_gain_buf[2] ? this.pitch_gain_buf[1] : (this.pitch_gain_buf[0] < this.pitch_gain_buf[2] ? this.pitch_gain_buf[2] : this.pitch_gain_buf[0])) : (this.pitch_gain_buf[2] < this.pitch_gain_buf[1] ? this.pitch_gain_buf[1] : (f = this.pitch_gain_buf[2] < this.pitch_gain_buf[0] ? this.pitch_gain_buf[2] : this.pitch_gain_buf[0]));
        if (f < this.last_pitch_gain) {
            this.last_pitch_gain = f;
        }
        if ((f2 = this.last_pitch_gain) > 0.95f) {
            f2 = 0.95f;
        }
        f2 *= f3;
        System.arraycopy(this.frmBuf, this.frameSize, this.frmBuf, 0, this.bufSize - this.frameSize);
        System.arraycopy(this.excBuf, this.frameSize, this.excBuf, 0, this.bufSize - this.frameSize);
        for (int i = 0; i < this.nbSubframes; ++i) {
            float f5;
            int n2 = this.subframeSize * i;
            int n3 = this.frmIdx + n2;
            int n4 = this.excIdx + n2;
            if (this.enhanced) {
                float f6;
                float f7;
                f5 = 0.9f;
                if (this.submodes[this.submodeID] != null) {
                    f7 = this.submodes[this.submodeID].lpc_enh_k1;
                    f6 = this.submodes[this.submodeID].lpc_enh_k2;
                } else {
                    f6 = 0.7f;
                    f7 = 0.7f;
                }
                float f8 = (1.0f - (1.0f - f5 * f7) / (1.0f - f5 * f6)) / f5;
                Filters.bw_lpc(f7, this.interp_qlpc, this.awk1, this.lpcSize);
                Filters.bw_lpc(f6, this.interp_qlpc, this.awk2, this.lpcSize);
                Filters.bw_lpc(f8, this.interp_qlpc, this.awk3, this.lpcSize);
            }
            f5 = 0.0f;
            for (n = 0; n < this.frameSize; ++n) {
                f5 += this.innov[n] * this.innov[n];
            }
            f5 = (float)Math.sqrt(f5 / (float)this.frameSize);
            for (n = 0; n < this.subframeSize; ++n) {
                this.excBuf[n4 + n] = f2 * this.excBuf[n4 + n - this.last_pitch] + f3 * (float)Math.sqrt(1.0f - f2) * 3.0f * f5 * (this.random.nextFloat() - 0.5f);
            }
            for (n = 0; n < this.subframeSize; ++n) {
                this.frmBuf[n3 + n] = this.excBuf[n4 + n];
            }
            if (this.enhanced) {
                Filters.filter_mem2(this.frmBuf, n3, this.awk2, this.awk1, this.subframeSize, this.lpcSize, this.mem_sp, this.lpcSize);
                Filters.filter_mem2(this.frmBuf, n3, this.awk3, this.interp_qlpc, this.subframeSize, this.lpcSize, this.mem_sp, 0);
                continue;
            }
            for (n = 0; n < this.lpcSize; ++n) {
                this.mem_sp[this.lpcSize + n] = 0.0f;
            }
            Filters.iir_mem2(this.frmBuf, n3, this.interp_qlpc, this.frmBuf, n3, this.subframeSize, this.lpcSize, this.mem_sp);
        }
        arrf[0] = this.frmBuf[0] + this.preemph * this.pre_mem;
        for (n = 1; n < this.frameSize; ++n) {
            arrf[n] = this.frmBuf[n] + this.preemph * arrf[n - 1];
        }
        this.pre_mem = arrf[this.frameSize - 1];
        this.first = 0;
        ++this.count_lost;
        this.pitch_gain_buf[this.pitch_gain_buf_idx++] = f2;
        if (this.pitch_gain_buf_idx > 2) {
            this.pitch_gain_buf_idx = 0;
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


/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.CbSearch;
import org.xiph.speex.Filters;
import org.xiph.speex.VQ;

public class SplitShapeSearch
extends CbSearch {
    public static final int MAX_COMPLEXITY = 10;
    private int subframesize;
    private int subvect_size;
    private int nb_subvect;
    private int[] shape_cb;
    private int shape_cb_size;
    private int shape_bits;
    private int have_sign;
    private int[] ind;
    private int[] signs;
    private float[] t;
    private float[] e;
    private float[] E;
    private float[] r2;
    private float[][] ot;
    private float[][] nt;
    private int[][] nind;
    private int[][] oind;

    public SplitShapeSearch(int n, int n2, int n3, int[] arrn, int n4, int n5) {
        this.subframesize = n;
        this.subvect_size = n2;
        this.nb_subvect = n3;
        this.shape_cb = arrn;
        this.shape_bits = n4;
        this.have_sign = n5;
        this.ind = new int[n3];
        this.signs = new int[n3];
        this.shape_cb_size = 1 << n4;
        this.ot = new float[10][n];
        this.nt = new float[10][n];
        this.oind = new int[10][n3];
        this.nind = new int[10][n3];
        this.t = new float[n];
        this.e = new float[n];
        this.r2 = new float[n];
        this.E = new float[this.shape_cb_size];
    }

    public final void quant(float[] arrf, float[] arrf2, float[] arrf3, float[] arrf4, int n, int n2, float[] arrf5, int n3, float[] arrf6, Bits bits, int n4) {
        int n5;
        int n6;
        int n7;
        int n8;
        int n9 = n4;
        if (n9 > 10) {
            n9 = 10;
        }
        float[] arrf7 = new float[this.shape_cb_size * this.subvect_size];
        int[] arrn = new int[n9];
        float[] arrf8 = new float[n9];
        float[] arrf9 = new float[n9];
        float[] arrf10 = new float[n9];
        for (n5 = 0; n5 < n9; ++n5) {
            for (n8 = 0; n8 < this.nb_subvect; ++n8) {
                this.oind[n5][n8] = -1;
                this.nind[n5][n8] = -1;
            }
        }
        for (n8 = 0; n8 < n9; ++n8) {
            for (n5 = 0; n5 < n2; ++n5) {
                this.ot[n8][n5] = arrf[n5];
            }
        }
        for (n5 = 0; n5 < this.shape_cb_size; ++n5) {
            n6 = n5 * this.subvect_size;
            int n10 = n5 * this.subvect_size;
            for (n8 = 0; n8 < this.subvect_size; ++n8) {
                arrf7[n6 + n8] = 0.0f;
                for (n7 = 0; n7 <= n8; ++n7) {
                    float[] arrf11 = arrf7;
                    int n11 = n6 + n8;
                    arrf11[n11] = (float)((double)arrf11[n11] + 0.03125 * (double)this.shape_cb[n10 + n7] * (double)arrf6[n8 - n7]);
                }
            }
            this.E[n5] = 0.0f;
            for (n8 = 0; n8 < this.subvect_size; ++n8) {
                float[] arrf12 = this.E;
                int n12 = n5;
                arrf12[n12] = arrf12[n12] + arrf7[n6 + n8] * arrf7[n6 + n8];
            }
        }
        for (n8 = 0; n8 < n9; ++n8) {
            arrf10[n8] = 0.0f;
        }
        for (n5 = 0; n5 < this.nb_subvect; ++n5) {
            int n13;
            n6 = n5 * this.subvect_size;
            for (n8 = 0; n8 < n9; ++n8) {
                arrf9[n8] = -1.0f;
            }
            for (n8 = 0; n8 < n9; ++n8) {
                if (this.have_sign != 0) {
                    VQ.nbest_sign(this.ot[n8], n6, arrf7, this.subvect_size, this.shape_cb_size, this.E, n9, arrn, arrf8);
                } else {
                    VQ.nbest(this.ot[n8], n6, arrf7, this.subvect_size, this.shape_cb_size, this.E, n9, arrn, arrf8);
                }
                block12 : for (n7 = 0; n7 < n9; ++n7) {
                    int n14;
                    int n15;
                    float f = 0.0f;
                    float[] arrf13 = this.ot[n8];
                    for (n13 = n6; n13 < n6 + this.subvect_size; ++n13) {
                        this.t[n13] = arrf13[n13];
                    }
                    float f2 = 1.0f;
                    int n16 = arrn[n7];
                    if (n16 >= this.shape_cb_size) {
                        f2 = -1.0f;
                        n16 -= this.shape_cb_size;
                    }
                    int n17 = n16 * this.subvect_size;
                    if (f2 > 0.0f) {
                        for (n13 = 0; n13 < this.subvect_size; ++n13) {
                            float[] arrf14 = this.t;
                            int n18 = n6 + n13;
                            arrf14[n18] = arrf14[n18] - arrf7[n17 + n13];
                        }
                    } else {
                        for (n13 = 0; n13 < this.subvect_size; ++n13) {
                            float[] arrf15 = this.t;
                            int n19 = n6 + n13;
                            arrf15[n19] = arrf15[n19] + arrf7[n17 + n13];
                        }
                    }
                    f = arrf10[n8];
                    for (n13 = n6; n13 < n6 + this.subvect_size; ++n13) {
                        f += this.t[n13] * this.t[n13];
                    }
                    if (f >= arrf9[n9 - 1] && (double)arrf9[n9 - 1] >= -0.5) continue;
                    for (n13 = n6 + this.subvect_size; n13 < n2; ++n13) {
                        this.t[n13] = arrf13[n13];
                    }
                    for (n13 = 0; n13 < this.subvect_size; ++n13) {
                        f2 = 1.0f;
                        n17 = arrn[n7];
                        if (n17 >= this.shape_cb_size) {
                            f2 = -1.0f;
                            n17 -= this.shape_cb_size;
                        }
                        float f3 = f2 * 0.03125f * (float)this.shape_cb[n17 * this.subvect_size + n13];
                        n15 = this.subvect_size - n13;
                        n14 = n6 + this.subvect_size;
                        while (n14 < n2) {
                            float[] arrf16 = this.t;
                            int n20 = n14++;
                            arrf16[n20] = arrf16[n20] - f3 * arrf6[n15];
                            ++n15;
                        }
                    }
                    for (n13 = 0; n13 < n9; ++n13) {
                        if (f >= arrf9[n13] && (double)arrf9[n13] >= -0.5) continue;
                        for (n14 = n9 - 1; n14 > n13; --n14) {
                            for (n15 = n6 + this.subvect_size; n15 < n2; ++n15) {
                                this.nt[n14][n15] = this.nt[n14 - 1][n15];
                            }
                            for (n15 = 0; n15 < this.nb_subvect; ++n15) {
                                this.nind[n14][n15] = this.nind[n14 - 1][n15];
                            }
                            arrf9[n14] = arrf9[n14 - 1];
                        }
                        for (n15 = n6 + this.subvect_size; n15 < n2; ++n15) {
                            this.nt[n13][n15] = this.t[n15];
                        }
                        for (n15 = 0; n15 < this.nb_subvect; ++n15) {
                            this.nind[n13][n15] = this.oind[n8][n15];
                        }
                        this.nind[n13][n5] = arrn[n7];
                        arrf9[n13] = f;
                        continue block12;
                    }
                }
                if (n5 == 0) break;
            }
            float[][] arrf17 = this.ot;
            this.ot = this.nt;
            this.nt = arrf17;
            for (n8 = 0; n8 < n9; ++n8) {
                for (n13 = 0; n13 < this.nb_subvect; ++n13) {
                    this.oind[n8][n13] = this.nind[n8][n13];
                }
            }
            for (n8 = 0; n8 < n9; ++n8) {
                arrf10[n8] = arrf9[n8];
            }
        }
        for (n5 = 0; n5 < this.nb_subvect; ++n5) {
            this.ind[n5] = this.nind[0][n5];
            bits.pack(this.ind[n5], this.shape_bits + this.have_sign);
        }
        for (n5 = 0; n5 < this.nb_subvect; ++n5) {
            float f = 1.0f;
            n6 = this.ind[n5];
            if (n6 >= this.shape_cb_size) {
                f = -1.0f;
                n6 -= this.shape_cb_size;
            }
            for (n8 = 0; n8 < this.subvect_size; ++n8) {
                this.e[this.subvect_size * n5 + n8] = f * 0.03125f * (float)this.shape_cb[n6 * this.subvect_size + n8];
            }
        }
        for (n8 = 0; n8 < n2; ++n8) {
            float[] arrf18 = arrf5;
            int n21 = n3 + n8;
            arrf18[n21] = arrf18[n21] + this.e[n8];
        }
        Filters.syn_percep_zero(this.e, 0, arrf2, arrf3, arrf4, this.r2, n2, n);
        for (n8 = 0; n8 < n2; ++n8) {
            float[] arrf19 = arrf;
            int n22 = n8;
            arrf19[n22] = arrf19[n22] - this.r2[n8];
        }
    }

    public final void unquant(float[] arrf, int n, int n2, Bits bits) {
        int n3;
        for (n3 = 0; n3 < this.nb_subvect; ++n3) {
            this.signs[n3] = this.have_sign != 0 ? bits.unpack(1) : 0;
            this.ind[n3] = bits.unpack(this.shape_bits);
        }
        for (n3 = 0; n3 < this.nb_subvect; ++n3) {
            float f = 1.0f;
            if (this.signs[n3] != 0) {
                f = -1.0f;
            }
            for (int i = 0; i < this.subvect_size; ++i) {
                float[] arrf2 = arrf;
                int n4 = n + this.subvect_size * n3 + i;
                arrf2[n4] = arrf2[n4] + f * 0.03125f * (float)this.shape_cb[this.ind[n3] * this.subvect_size + i];
            }
        }
    }
}


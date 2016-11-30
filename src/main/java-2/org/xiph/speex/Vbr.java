/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class Vbr {
    public static final int VBR_MEMORY_SIZE = 5;
    public static final int MIN_ENERGY = 6000;
    public static final float NOISE_POW = 0.3f;
    public static final float[][] nb_thresh = new float[][]{{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f}, {3.5f, 2.5f, 2.0f, 1.2f, 0.5f, 0.0f, -0.5f, -0.7f, -0.8f, -0.9f, -1.0f}, {10.0f, 6.5f, 5.2f, 4.5f, 3.9f, 3.5f, 3.0f, 2.5f, 2.3f, 1.8f, 1.0f}, {11.0f, 8.8f, 7.5f, 6.5f, 5.0f, 3.9f, 3.9f, 3.9f, 3.5f, 3.0f, 1.0f}, {11.0f, 11.0f, 9.9f, 9.0f, 8.0f, 7.0f, 6.5f, 6.0f, 5.0f, 4.0f, 2.0f}, {11.0f, 11.0f, 11.0f, 11.0f, 9.5f, 9.0f, 8.0f, 7.0f, 6.5f, 5.0f, 3.0f}, {11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 9.5f, 8.5f, 8.0f, 6.5f, 4.0f}, {11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 9.8f, 7.5f, 5.5f}, {8.0f, 5.0f, 3.7f, 3.0f, 2.5f, 2.0f, 1.8f, 1.5f, 1.0f, 0.0f, 0.0f}};
    public static final float[][] hb_thresh = new float[][]{{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f}, {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f}, {11.0f, 11.0f, 9.5f, 8.5f, 7.5f, 6.0f, 5.0f, 3.9f, 3.0f, 2.0f, 1.0f}, {11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 9.5f, 8.7f, 7.8f, 7.0f, 6.5f, 4.0f}, {11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 11.0f, 9.8f, 7.5f, 5.5f}};
    public static final float[][] uhb_thresh = new float[][]{{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f}, {3.9f, 2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f}};
    private float energy_alpha = 0.1f;
    private float average_energy = 0.0f;
    private float last_energy = 1.0f;
    private float[] last_log_energy;
    private float accum_sum = 0.0f;
    private float last_pitch_coef = 0.0f;
    private float soft_pitch = 0.0f;
    private float last_quality = 0.0f;
    private float noise_level;
    private float noise_accum = (float)(0.05 * Math.pow(6000.0, 0.30000001192092896));
    private float noise_accum_count = 0.05f;
    private int consec_noise;

    public Vbr() {
        this.noise_level = this.noise_accum / this.noise_accum_count;
        this.consec_noise = 0;
        this.last_log_energy = new float[5];
        for (int i = 0; i < 5; ++i) {
            this.last_log_energy[i] = (float)Math.log(6000.0);
        }
    }

    public float analysis(float[] arrf, int n, int n2, float f) {
        float f2;
        int n3;
        boolean bl;
        float f3 = 0.0f;
        float f4 = 0.0f;
        float f5 = 0.0f;
        float f6 = 7.0f;
        float f7 = 0.0f;
        for (n3 = 0; n3 < n >> 1; ++n3) {
            f4 += arrf[n3] * arrf[n3];
        }
        for (n3 = n >> 1; n3 < n; ++n3) {
            f5 += arrf[n3] * arrf[n3];
        }
        f3 = f4 + f5;
        float f8 = (float)Math.log(f3 + 6000.0f);
        for (n3 = 0; n3 < 5; ++n3) {
            f7 += (f8 - this.last_log_energy[n3]) * (f8 - this.last_log_energy[n3]);
        }
        if ((f7 /= 150.0f) > 1.0f) {
            f7 = 1.0f;
        }
        float f9 = 3.0f * (f - 0.4f) * Math.abs(f - 0.4f);
        this.average_energy = (1.0f - this.energy_alpha) * this.average_energy + this.energy_alpha * f3;
        this.noise_level = this.noise_accum / this.noise_accum_count;
        float f10 = (float)Math.pow(f3, 0.30000001192092896);
        if (this.noise_accum_count < 0.06f && f3 > 6000.0f) {
            this.noise_accum = 0.05f * f10;
        }
        if (f9 < 0.3f && f7 < 0.2f && f10 < 1.2f * this.noise_level || f9 < 0.3f && f7 < 0.05f && f10 < 1.5f * this.noise_level || f9 < 0.4f && f7 < 0.05f && f10 < 1.2f * this.noise_level || f9 < 0.0f && f7 < 0.05f) {
            bl = false;
            ++this.consec_noise;
            f2 = f10 > 3.0f * this.noise_level ? 3.0f * this.noise_level : f10;
            if (this.consec_noise >= 4) {
                this.noise_accum = 0.95f * this.noise_accum + 0.05f * f2;
                this.noise_accum_count = 0.95f * this.noise_accum_count + 0.05f;
            }
        } else {
            bl = true;
            this.consec_noise = 0;
        }
        if (f10 < this.noise_level && f3 > 6000.0f) {
            this.noise_accum = 0.95f * this.noise_accum + 0.05f * f10;
            this.noise_accum_count = 0.95f * this.noise_accum_count + 0.05f;
        }
        if (f3 < 30000.0f) {
            f6 -= 0.7f;
            if (f3 < 10000.0f) {
                f6 -= 0.7f;
            }
            if (f3 < 3000.0f) {
                f6 -= 0.7f;
            }
        } else {
            f2 = (float)Math.log((f3 + 1.0f) / (1.0f + this.last_energy));
            float f11 = (float)Math.log((f3 + 1.0f) / (1.0f + this.average_energy));
            if (f11 < -5.0f) {
                f11 = -5.0f;
            }
            if (f11 > 2.0f) {
                f11 = 2.0f;
            }
            if (f11 > 0.0f) {
                f6 += 0.6f * f11;
            }
            if (f11 < 0.0f) {
                f6 += 0.5f * f11;
            }
            if (f2 > 0.0f) {
                if (f2 > 5.0f) {
                    f2 = 5.0f;
                }
                f6 += 0.5f * f2;
            }
            if (f5 > 1.6f * f4) {
                f6 += 0.5f;
            }
        }
        this.last_energy = f3;
        this.soft_pitch = 0.6f * this.soft_pitch + 0.4f * f;
        if ((f6 = (float)((double)f6 + 2.200000047683716 * ((double)f - 0.4 + ((double)this.soft_pitch - 0.4)))) < this.last_quality) {
            f6 = 0.5f * f6 + 0.5f * this.last_quality;
        }
        if (f6 < 4.0f) {
            f6 = 4.0f;
        }
        if (f6 > 10.0f) {
            f6 = 10.0f;
        }
        if (this.consec_noise >= 3) {
            f6 = 4.0f;
        }
        if (this.consec_noise != 0) {
            f6 -= (float)(1.0 * (Math.log(3.0 + (double)this.consec_noise) - Math.log(3.0)));
        }
        if (f6 < 0.0f) {
            f6 = 0.0f;
        }
        if (f3 < 60000.0f) {
            if (this.consec_noise > 2) {
                f6 -= (float)(0.5 * (Math.log(3.0 + (double)this.consec_noise) - Math.log(3.0)));
            }
            if (f3 < 10000.0f && this.consec_noise > 2) {
                f6 -= (float)(0.5 * (Math.log(3.0 + (double)this.consec_noise) - Math.log(3.0)));
            }
            if (f6 < 0.0f) {
                f6 = 0.0f;
            }
            f6 += (float)(0.3 * Math.log((double)f3 / 60000.0));
        }
        if (f6 < -1.0f) {
            f6 = -1.0f;
        }
        this.last_pitch_coef = f;
        this.last_quality = f6;
        for (n3 = 4; n3 > 0; --n3) {
            this.last_log_energy[n3] = this.last_log_energy[n3 - 1];
        }
        this.last_log_energy[0] = f8;
        return f6;
    }
}


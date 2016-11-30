/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;

public interface Encoder {
    public int encode(Bits var1, float[] var2);

    public int getEncodedFrameSize();

    public int getFrameSize();

    public void setQuality(int var1);

    public int getBitRate();

    public float[] getPiGain();

    public float[] getExc();

    public float[] getInnov();

    public void setMode(int var1);

    public int getMode();

    public void setBitRate(int var1);

    public void setVbr(boolean var1);

    public boolean getVbr();

    public void setVad(boolean var1);

    public boolean getVad();

    public void setDtx(boolean var1);

    public boolean getDtx();

    public int getAbr();

    public void setAbr(int var1);

    public void setVbrQuality(float var1);

    public float getVbrQuality();

    public void setComplexity(int var1);

    public int getComplexity();

    public void setSamplingRate(int var1);

    public int getSamplingRate();

    public int getLookAhead();

    public float getRelativeQuality();
}


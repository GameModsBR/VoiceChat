package org.xiph.speex;

public interface Encoder {

    int encode(Bits var1, float[] var2);

    int getEncodedFrameSize();

    int getFrameSize();

    void setQuality(int var1);

    int getBitRate();

    void setBitRate(int var1);

    float[] getPiGain();

    float[] getExc();

    float[] getInnov();

    int getMode();

    void setMode(int var1);

    boolean getVbr();

    void setVbr(boolean var1);

    boolean getVad();

    void setVad(boolean var1);

    boolean getDtx();

    void setDtx(boolean var1);

    int getAbr();

    void setAbr(int var1);

    float getVbrQuality();

    void setVbrQuality(float var1);

    int getComplexity();

    void setComplexity(int var1);

    int getSamplingRate();

    void setSamplingRate(int var1);

    int getLookAhead();

    float getRelativeQuality();
}

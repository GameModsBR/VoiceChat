/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;
import org.xiph.speex.spi.Pcm2SpeexAudioInputStream;
import org.xiph.speex.spi.Speex2PcmAudioInputStream;
import org.xiph.speex.spi.SpeexEncoding;

public class SpeexFormatConvertionProvider
extends FormatConversionProvider {
    public static final AudioFormat.Encoding[] NO_ENCODING = new AudioFormat.Encoding[0];
    public static final AudioFormat.Encoding[] PCM_ENCODING = new AudioFormat.Encoding[]{AudioFormat.Encoding.PCM_SIGNED};
    public static final AudioFormat.Encoding[] SPEEX_ENCODING = new AudioFormat.Encoding[]{SpeexEncoding.SPEEX};
    public static final AudioFormat.Encoding[] BOTH_ENCODINGS = new AudioFormat.Encoding[]{SpeexEncoding.SPEEX, AudioFormat.Encoding.PCM_SIGNED};
    public static final AudioFormat[] NO_FORMAT = new AudioFormat[0];

    public AudioFormat.Encoding[] getSourceEncodings() {
        AudioFormat.Encoding[] arrencoding = new AudioFormat.Encoding[]{SpeexEncoding.SPEEX, AudioFormat.Encoding.PCM_SIGNED};
        return arrencoding;
    }

    public AudioFormat.Encoding[] getTargetEncodings() {
        AudioFormat.Encoding[] arrencoding = new AudioFormat.Encoding[]{SpeexEncoding.SPEEX_Q0, SpeexEncoding.SPEEX_Q1, SpeexEncoding.SPEEX_Q2, SpeexEncoding.SPEEX_Q3, SpeexEncoding.SPEEX_Q4, SpeexEncoding.SPEEX_Q5, SpeexEncoding.SPEEX_Q6, SpeexEncoding.SPEEX_Q7, SpeexEncoding.SPEEX_Q8, SpeexEncoding.SPEEX_Q9, SpeexEncoding.SPEEX_Q10, SpeexEncoding.SPEEX_VBR0, SpeexEncoding.SPEEX_VBR1, SpeexEncoding.SPEEX_VBR2, SpeexEncoding.SPEEX_VBR3, SpeexEncoding.SPEEX_VBR4, SpeexEncoding.SPEEX_VBR5, SpeexEncoding.SPEEX_VBR6, SpeexEncoding.SPEEX_VBR7, SpeexEncoding.SPEEX_VBR8, SpeexEncoding.SPEEX_VBR9, SpeexEncoding.SPEEX_VBR10, AudioFormat.Encoding.PCM_SIGNED};
        return arrencoding;
    }

    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat audioFormat) {
        if (audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
            AudioFormat.Encoding[] arrencoding = new AudioFormat.Encoding[]{SpeexEncoding.SPEEX_Q0, SpeexEncoding.SPEEX_Q1, SpeexEncoding.SPEEX_Q2, SpeexEncoding.SPEEX_Q3, SpeexEncoding.SPEEX_Q4, SpeexEncoding.SPEEX_Q5, SpeexEncoding.SPEEX_Q6, SpeexEncoding.SPEEX_Q7, SpeexEncoding.SPEEX_Q8, SpeexEncoding.SPEEX_Q9, SpeexEncoding.SPEEX_Q10, SpeexEncoding.SPEEX_VBR0, SpeexEncoding.SPEEX_VBR1, SpeexEncoding.SPEEX_VBR2, SpeexEncoding.SPEEX_VBR3, SpeexEncoding.SPEEX_VBR4, SpeexEncoding.SPEEX_VBR5, SpeexEncoding.SPEEX_VBR6, SpeexEncoding.SPEEX_VBR7, SpeexEncoding.SPEEX_VBR8, SpeexEncoding.SPEEX_VBR9, SpeexEncoding.SPEEX_VBR10};
            return arrencoding;
        }
        if (audioFormat.getEncoding() instanceof SpeexEncoding) {
            AudioFormat.Encoding[] arrencoding = new AudioFormat.Encoding[]{AudioFormat.Encoding.PCM_SIGNED};
            return arrencoding;
        }
        AudioFormat.Encoding[] arrencoding = new AudioFormat.Encoding[]{};
        return arrencoding;
    }

    public AudioFormat[] getTargetFormats(AudioFormat.Encoding encoding, AudioFormat audioFormat) {
        if (audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && encoding instanceof SpeexEncoding) {
            if (audioFormat.getChannels() > 2 || audioFormat.getChannels() <= 0 || audioFormat.isBigEndian()) {
                AudioFormat[] arraudioFormat = new AudioFormat[]{};
                return arraudioFormat;
            }
            AudioFormat[] arraudioFormat = new AudioFormat[]{new AudioFormat(encoding, audioFormat.getSampleRate(), -1, audioFormat.getChannels(), -1, -1.0f, false)};
            return arraudioFormat;
        }
        if (audioFormat.getEncoding() instanceof SpeexEncoding && encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            AudioFormat[] arraudioFormat = new AudioFormat[]{new AudioFormat(audioFormat.getSampleRate(), 16, audioFormat.getChannels(), true, false)};
            return arraudioFormat;
        }
        AudioFormat[] arraudioFormat = new AudioFormat[]{};
        return arraudioFormat;
    }

    public AudioInputStream getAudioInputStream(AudioFormat.Encoding encoding, AudioInputStream audioInputStream) {
        if (this.isConversionSupported(encoding, audioInputStream.getFormat())) {
            AudioFormat[] arraudioFormat = this.getTargetFormats(encoding, audioInputStream.getFormat());
            if (arraudioFormat != null && arraudioFormat.length > 0) {
                AudioFormat audioFormat;
                AudioFormat audioFormat2 = audioInputStream.getFormat();
                if (audioFormat2.equals(audioFormat = arraudioFormat[0])) {
                    return audioInputStream;
                }
                if (audioFormat2.getEncoding() instanceof SpeexEncoding && audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
                    return new Speex2PcmAudioInputStream(audioInputStream, audioFormat, -1);
                }
                if (audioFormat2.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && audioFormat.getEncoding() instanceof SpeexEncoding) {
                    return new Pcm2SpeexAudioInputStream(audioInputStream, audioFormat, -1);
                }
                throw new IllegalArgumentException("unable to convert " + audioFormat2.toString() + " to " + audioFormat.toString());
            }
            throw new IllegalArgumentException("target format not found");
        }
        throw new IllegalArgumentException("conversion not supported");
    }

    public AudioInputStream getAudioInputStream(AudioFormat audioFormat, AudioInputStream audioInputStream) {
        if (this.isConversionSupported(audioFormat, audioInputStream.getFormat())) {
            AudioFormat[] arraudioFormat = this.getTargetFormats(audioFormat.getEncoding(), audioInputStream.getFormat());
            if (arraudioFormat != null && arraudioFormat.length > 0) {
                AudioFormat audioFormat2 = audioInputStream.getFormat();
                if (audioFormat2.equals(audioFormat)) {
                    return audioInputStream;
                }
                if (audioFormat2.getEncoding() instanceof SpeexEncoding && audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
                    return new Speex2PcmAudioInputStream(audioInputStream, audioFormat, -1);
                }
                if (audioFormat2.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && audioFormat.getEncoding() instanceof SpeexEncoding) {
                    return new Pcm2SpeexAudioInputStream(audioInputStream, audioFormat, -1);
                }
                throw new IllegalArgumentException("unable to convert " + audioFormat2.toString() + " to " + audioFormat.toString());
            }
            throw new IllegalArgumentException("target format not found");
        }
        throw new IllegalArgumentException("conversion not supported");
    }
}


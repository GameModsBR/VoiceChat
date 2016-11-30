/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.AudioFileWriter;
import org.xiph.speex.spi.SpeexEncoding;
import org.xiph.speex.spi.SpeexFileFormatType;

public class SpeexAudioFileWriter
extends AudioFileWriter {
    public static final AudioFileFormat.Type[] NO_FORMAT = new AudioFileFormat.Type[0];
    public static final AudioFileFormat.Type[] SPEEX_FORMAT = new AudioFileFormat.Type[]{SpeexFileFormatType.SPEEX};

    public AudioFileFormat.Type[] getAudioFileTypes() {
        return SPEEX_FORMAT;
    }

    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream audioInputStream) {
        if (audioInputStream.getFormat().getEncoding() instanceof SpeexEncoding) {
            return SPEEX_FORMAT;
        }
        return NO_FORMAT;
    }

    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type type, OutputStream outputStream) throws IOException {
        AudioFileFormat.Type[] arrtype = this.getAudioFileTypes(audioInputStream);
        if (arrtype != null && arrtype.length > 0) {
            return this.write(audioInputStream, outputStream);
        }
        throw new IllegalArgumentException("cannot write given file type");
    }

    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type type, File file) throws IOException {
        AudioFileFormat.Type[] arrtype = this.getAudioFileTypes(audioInputStream);
        if (arrtype != null && arrtype.length > 0) {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            return this.write(audioInputStream, fileOutputStream);
        }
        throw new IllegalArgumentException("cannot write given file type");
    }

    private int write(AudioInputStream audioInputStream, OutputStream outputStream) throws IOException {
        int n;
        byte[] arrby = new byte[2048];
        int n2 = 0;
        while ((n = audioInputStream.read(arrby, 0, 2048)) > 0) {
            outputStream.write(arrby, 0, n);
            n2 += n;
        }
        outputStream.flush();
        outputStream.close();
        return n2;
    }
}


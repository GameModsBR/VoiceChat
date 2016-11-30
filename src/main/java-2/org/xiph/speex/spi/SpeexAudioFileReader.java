/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import org.xiph.speex.OggCrc;
import org.xiph.speex.spi.SpeexEncoding;
import org.xiph.speex.spi.SpeexFileFormatType;

public class SpeexAudioFileReader
extends AudioFileReader {
    public static final int OGG_HEADERSIZE = 27;
    public static final int SPEEX_HEADERSIZE = 80;
    public static final int SEGOFFSET = 26;
    public static final String OGGID = "OggS";
    public static final String SPEEXID = "Speex   ";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            AudioFileFormat audioFileFormat = this.getAudioFileFormat(inputStream, (int)file.length());
            return audioFileFormat;
        }
        finally {
            inputStream.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AudioFileFormat getAudioFileFormat(URL uRL) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = uRL.openStream();
        try {
            AudioFileFormat audioFileFormat = this.getAudioFileFormat(inputStream);
            return audioFileFormat;
        }
        finally {
            inputStream.close();
        }
    }

    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        return this.getAudioFileFormat(inputStream, -1);
    }

    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, int n) throws UnsupportedAudioFileException, IOException {
        return this.getAudioFileFormat(inputStream, null, n);
    }

    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, ByteArrayOutputStream byteArrayOutputStream, int n) throws UnsupportedAudioFileException, IOException {
        AudioFormat audioFormat;
        try {
            boolean bl;
            if (inputStream.markSupported()) {
                inputStream.mark(675);
            }
            int n2 = -1;
            int n3 = 0;
            int n4 = 0;
            int n5 = -1;
            float f = -1.0f;
            byte[] arrby = new byte[128];
            int n6 = 0;
            int n7 = 0;
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            if (byteArrayOutputStream == null) {
                byteArrayOutputStream = new ByteArrayOutputStream(128);
            }
            dataInputStream.readFully(arrby, 0, 27);
            byteArrayOutputStream.write(arrby, 0, 27);
            int n8 = SpeexAudioFileReader.readInt(arrby, 22);
            arrby[22] = 0;
            arrby[23] = 0;
            arrby[24] = 0;
            arrby[25] = 0;
            int n9 = OggCrc.checksum(0, arrby, 0, 27);
            if (!"OggS".equals(new String(arrby, 0, 4))) {
                throw new UnsupportedAudioFileException("missing ogg id!");
            }
            n6 = arrby[26] & 255;
            if (n6 > 1) {
                throw new UnsupportedAudioFileException("Corrupt Speex Header: more than 1 segments");
            }
            dataInputStream.readFully(arrby, 27, n6);
            byteArrayOutputStream.write(arrby, 27, n6);
            n9 = OggCrc.checksum(n9, arrby, 27, n6);
            n7 = arrby[27] & 255;
            if (n7 != 80) {
                throw new UnsupportedAudioFileException("Corrupt Speex Header: size=" + n7);
            }
            dataInputStream.readFully(arrby, 28, n7);
            byteArrayOutputStream.write(arrby, 28, n7);
            n9 = OggCrc.checksum(n9, arrby, 28, n7);
            if (!"Speex   ".equals(new String(arrby, 28, 8))) {
                throw new UnsupportedAudioFileException("Corrupt Speex Header: missing Speex ID");
            }
            n2 = SpeexAudioFileReader.readInt(arrby, 68);
            n3 = SpeexAudioFileReader.readInt(arrby, 64);
            n4 = SpeexAudioFileReader.readInt(arrby, 76);
            int n10 = SpeexAudioFileReader.readInt(arrby, 92);
            boolean bl2 = bl = SpeexAudioFileReader.readInt(arrby, 88) == 1;
            if (n9 != n8) {
                throw new IOException("Ogg CheckSums do not match");
            }
            if (!bl) {
                // empty if block
            }
            if (n2 >= 0 && n2 <= 2 && n10 > 0) {
                f = (float)n3 / ((n2 == 0 ? 160.0f : (n2 == 1 ? 320.0f : 640.0f)) * (float)n10);
            }
            audioFormat = new AudioFormat(SpeexEncoding.SPEEX, n3, -1, n4, n5, f, false);
        }
        catch (UnsupportedAudioFileException var5_5) {
            if (inputStream.markSupported()) {
                inputStream.reset();
            }
            throw var5_5;
        }
        catch (IOException var5_6) {
            if (inputStream.markSupported()) {
                inputStream.reset();
            }
            throw new UnsupportedAudioFileException(var5_6.getMessage());
        }
        return new AudioFileFormat(SpeexFileFormatType.SPEEX, audioFormat, -1);
    }

    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            return this.getAudioInputStream(fileInputStream, (int)file.length());
        }
        catch (UnsupportedAudioFileException var3_3) {
            fileInputStream.close();
            throw var3_3;
        }
        catch (IOException var3_4) {
            fileInputStream.close();
            throw var3_4;
        }
    }

    public AudioInputStream getAudioInputStream(URL uRL) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = uRL.openStream();
        try {
            return this.getAudioInputStream(inputStream);
        }
        catch (UnsupportedAudioFileException var3_3) {
            inputStream.close();
            throw var3_3;
        }
        catch (IOException var3_4) {
            inputStream.close();
            throw var3_4;
        }
    }

    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        return this.getAudioInputStream(inputStream, -1);
    }

    protected AudioInputStream getAudioInputStream(InputStream inputStream, int n) throws UnsupportedAudioFileException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(128);
        AudioFileFormat audioFileFormat = this.getAudioFileFormat(inputStream, byteArrayOutputStream, n);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        SequenceInputStream sequenceInputStream = new SequenceInputStream(byteArrayInputStream, inputStream);
        return new AudioInputStream(sequenceInputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
    }

    private static int readInt(byte[] arrby, int n) {
        return arrby[n] & 255 | (arrby[n + 1] & 255) << 8 | (arrby[n + 2] & 255) << 16 | arrby[n + 3] << 24;
    }
}


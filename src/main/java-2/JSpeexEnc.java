/*
 * Decompiled with CFR 0_118.
 */
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.Encoder;
import org.xiph.speex.OggSpeexWriter;
import org.xiph.speex.PcmWaveWriter;
import org.xiph.speex.RawWriter;
import org.xiph.speex.SpeexEncoder;

public class JSpeexEnc {
    public static final String VERSION = "Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)";
    public static final String COPYRIGHT = "Copyright (C) 2002-2004 Wimba S.A.";
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;
    protected int printlevel = 1;
    public static final int FILE_FORMAT_RAW = 0;
    public static final int FILE_FORMAT_OGG = 1;
    public static final int FILE_FORMAT_WAVE = 2;
    protected int srcFormat = 1;
    protected int destFormat = 2;
    protected int mode = -1;
    protected int quality = 8;
    protected int complexity = 3;
    protected int nframes = 1;
    protected int bitrate = -1;
    protected int sampleRate = -1;
    protected int channels = 1;
    protected float vbr_quality = -1.0f;
    protected boolean vbr = false;
    protected boolean vad = false;
    protected boolean dtx = false;
    protected String srcFile;
    protected String destFile;

    public static void main(String[] arrstring) throws IOException {
        JSpeexEnc jSpeexEnc = new JSpeexEnc();
        if (jSpeexEnc.parseArgs(arrstring)) {
            jSpeexEnc.encode();
        }
    }

    public boolean parseArgs(String[] arrstring) {
        if (arrstring.length < 2) {
            if (arrstring.length == 1 && (arrstring[0].equalsIgnoreCase("-v") || arrstring[0].equalsIgnoreCase("--version"))) {
                JSpeexEnc.version();
                return false;
            }
            JSpeexEnc.usage();
            return false;
        }
        this.srcFile = arrstring[arrstring.length - 2];
        this.destFile = arrstring[arrstring.length - 1];
        this.srcFormat = this.srcFile.toLowerCase().endsWith(".wav") ? 2 : 0;
        this.destFormat = this.destFile.toLowerCase().endsWith(".spx") ? 1 : (this.destFile.toLowerCase().endsWith(".wav") ? 2 : 0);
        for (int i = 0; i < arrstring.length - 2; ++i) {
            if (arrstring[i].equalsIgnoreCase("-h") || arrstring[i].equalsIgnoreCase("--help")) {
                JSpeexEnc.usage();
                return false;
            }
            if (arrstring[i].equalsIgnoreCase("-v") || arrstring[i].equalsIgnoreCase("--version")) {
                JSpeexEnc.version();
                return false;
            }
            if (arrstring[i].equalsIgnoreCase("--verbose")) {
                this.printlevel = 0;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--quiet")) {
                this.printlevel = 2;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("-n") || arrstring[i].equalsIgnoreCase("-nb") || arrstring[i].equalsIgnoreCase("--narrowband")) {
                this.mode = 0;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("-w") || arrstring[i].equalsIgnoreCase("-wb") || arrstring[i].equalsIgnoreCase("--wideband")) {
                this.mode = 1;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("-u") || arrstring[i].equalsIgnoreCase("-uwb") || arrstring[i].equalsIgnoreCase("--ultra-wideband")) {
                this.mode = 2;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("-q") || arrstring[i].equalsIgnoreCase("--quality")) {
                try {
                    this.vbr_quality = Float.parseFloat(arrstring[++i]);
                    this.quality = (int)this.vbr_quality;
                    continue;
                }
                catch (NumberFormatException var3_3) {
                    JSpeexEnc.usage();
                    return false;
                }
            }
            if (arrstring[i].equalsIgnoreCase("--complexity")) {
                try {
                    this.complexity = Integer.parseInt(arrstring[++i]);
                    continue;
                }
                catch (NumberFormatException var3_4) {
                    JSpeexEnc.usage();
                    return false;
                }
            }
            if (arrstring[i].equalsIgnoreCase("--nframes")) {
                try {
                    this.nframes = Integer.parseInt(arrstring[++i]);
                    continue;
                }
                catch (NumberFormatException var3_5) {
                    JSpeexEnc.usage();
                    return false;
                }
            }
            if (arrstring[i].equalsIgnoreCase("--vbr")) {
                this.vbr = true;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--vad")) {
                this.vad = true;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--dtx")) {
                this.dtx = true;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--rate")) {
                try {
                    this.sampleRate = Integer.parseInt(arrstring[++i]);
                    continue;
                }
                catch (NumberFormatException var3_6) {
                    JSpeexEnc.usage();
                    return false;
                }
            }
            if (arrstring[i].equalsIgnoreCase("--stereo")) {
                this.channels = 2;
                continue;
            }
            JSpeexEnc.usage();
            return false;
        }
        return true;
    }

    public static void usage() {
        JSpeexEnc.version();
        System.out.println("");
        System.out.println("Usage: JSpeexEnc [options] input_file output_file");
        System.out.println("Where:");
        System.out.println("  input_file can be:");
        System.out.println("    filename.wav  a PCM wav file");
        System.out.println("    filename.*    a raw PCM file (any extension other than .wav)");
        System.out.println("  output_file can be:");
        System.out.println("    filename.spx  an Ogg Speex file");
        System.out.println("    filename.wav  a Wave Speex file (beta!!!)");
        System.out.println("    filename.*    a raw Speex file");
        System.out.println("Options: -h, --help     This help");
        System.out.println("         -v, --version  Version information");
        System.out.println("         --verbose      Print detailed information");
        System.out.println("         --quiet        Print minimal information");
        System.out.println("         -n, -nb        Consider input as Narrowband (8kHz)");
        System.out.println("         -w, -wb        Consider input as Wideband (16kHz)");
        System.out.println("         -u, -uwb       Consider input as Ultra-Wideband (32kHz)");
        System.out.println("         --quality n    Encoding quality (0-10) default 8");
        System.out.println("         --complexity n Encoding complexity (0-10) default 3");
        System.out.println("         --nframes n    Number of frames per Ogg packet, default 1");
        System.out.println("         --vbr          Enable varible bit-rate (VBR)");
        System.out.println("         --vad          Enable voice activity detection (VAD)");
        System.out.println("         --dtx          Enable file based discontinuous transmission (DTX)");
        System.out.println("         if the input file is raw PCM (not a Wave file)");
        System.out.println("         --rate n       Sampling rate for raw input");
        System.out.println("         --stereo       Consider input as stereo");
        System.out.println("More information is available from: http://jspeex.sourceforge.net/");
        System.out.println("This code is a Java port of the Speex codec: http://www.speex.org/");
    }

    public static void version() {
        System.out.println("Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)");
        System.out.println("using Java Speex Encoder v0.9.7 ($Revision: 1.6 $)");
        System.out.println("Copyright (C) 2002-2004 Wimba S.A.");
    }

    public void encode() throws IOException {
        this.encode(new File(this.srcFile), new File(this.destFile));
    }

    public void encode(File file, File file2) throws IOException {
        Object object;
        AudioFileWriter audioFileWriter;
        byte[] arrby = new byte[2560];
        if (this.printlevel <= 1) {
            JSpeexEnc.version();
        }
        if (this.printlevel <= 0) {
            System.out.println("");
        }
        if (this.printlevel <= 0) {
            System.out.println("Input File: " + file);
        }
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        if (this.srcFormat == 2) {
            dataInputStream.readFully(arrby, 0, 12);
            if (!"RIFF".equals(new String(arrby, 0, 4)) && !"WAVE".equals(new String(arrby, 8, 4))) {
                System.err.println("Not a WAVE file");
                return;
            }
            dataInputStream.readFully(arrby, 0, 8);
            object = new String(arrby, 0, 4);
            int n = JSpeexEnc.readInt(arrby, 4);
            while (!object.equals("data")) {
                dataInputStream.readFully(arrby, 0, n);
                if (object.equals("fmt ")) {
                    if (JSpeexEnc.readShort(arrby, 0) != 1) {
                        System.err.println("Not a PCM file");
                        return;
                    }
                    this.channels = JSpeexEnc.readShort(arrby, 2);
                    this.sampleRate = JSpeexEnc.readInt(arrby, 4);
                    if (JSpeexEnc.readShort(arrby, 14) != 16) {
                        System.err.println("Not a 16 bit file " + JSpeexEnc.readShort(arrby, 18));
                        return;
                    }
                    if (this.printlevel <= 0) {
                        System.out.println("File Format: PCM wave");
                        System.out.println("Sample Rate: " + this.sampleRate);
                        System.out.println("Channels: " + this.channels);
                    }
                }
                dataInputStream.readFully(arrby, 0, 8);
                object = new String(arrby, 0, 4);
                n = JSpeexEnc.readInt(arrby, 4);
            }
            if (this.printlevel <= 0) {
                System.out.println("Data size: " + n);
            }
        } else {
            if (this.sampleRate < 0) {
                switch (this.mode) {
                    case 0: {
                        this.sampleRate = 8000;
                        break;
                    }
                    case 1: {
                        this.sampleRate = 16000;
                        break;
                    }
                    case 2: {
                        this.sampleRate = 32000;
                        break;
                    }
                    default: {
                        this.sampleRate = 8000;
                    }
                }
            }
            if (this.printlevel <= 0) {
                System.out.println("File format: Raw audio");
                System.out.println("Sample rate: " + this.sampleRate);
                System.out.println("Channels: " + this.channels);
                System.out.println("Data size: " + file.length());
            }
        }
        if (this.mode < 0) {
            if (this.sampleRate < 100) {
                this.sampleRate *= 1000;
            }
            this.mode = this.sampleRate < 12000 ? 0 : (this.sampleRate < 24000 ? 1 : 2);
        }
        object = new SpeexEncoder();
        object.init(this.mode, this.quality, this.sampleRate, this.channels);
        if (this.complexity > 0) {
            object.getEncoder().setComplexity(this.complexity);
        }
        if (this.bitrate > 0) {
            object.getEncoder().setBitRate(this.bitrate);
        }
        if (this.vbr) {
            object.getEncoder().setVbr(this.vbr);
            if (this.vbr_quality > 0.0f) {
                object.getEncoder().setVbrQuality(this.vbr_quality);
            }
        }
        if (this.vad) {
            object.getEncoder().setVad(this.vad);
        }
        if (this.dtx) {
            object.getEncoder().setDtx(this.dtx);
        }
        if (this.printlevel <= 0) {
            System.out.println("");
            System.out.println("Output File: " + file2);
            System.out.println("File format: Ogg Speex");
            System.out.println("Encoder mode: " + (this.mode == 0 ? "Narrowband" : (this.mode == 1 ? "Wideband" : "UltraWideband")));
            System.out.println("Quality: " + (this.vbr ? this.vbr_quality : (float)this.quality));
            System.out.println("Complexity: " + this.complexity);
            System.out.println("Frames per packet: " + this.nframes);
            System.out.println("Varible bitrate: " + this.vbr);
            System.out.println("Voice activity detection: " + this.vad);
            System.out.println("Discontinouous Transmission: " + this.dtx);
        }
        if (this.destFormat == 1) {
            audioFileWriter = new OggSpeexWriter(this.mode, this.sampleRate, this.channels, this.nframes, this.vbr);
        } else if (this.destFormat == 2) {
            this.nframes = PcmWaveWriter.WAVE_FRAME_SIZES[this.mode - 1][this.channels - 1][this.quality];
            audioFileWriter = new PcmWaveWriter(this.mode, this.quality, this.sampleRate, this.channels, this.nframes, this.vbr);
        } else {
            audioFileWriter = new RawWriter();
        }
        audioFileWriter.open(file2);
        audioFileWriter.writeHeader("Encoded with: Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)");
        int n = 2 * this.channels * object.getFrameSize();
        try {
            do {
                int n2;
                dataInputStream.readFully(arrby, 0, this.nframes * n);
                for (n2 = 0; n2 < this.nframes; ++n2) {
                    object.processData(arrby, n2 * n, n);
                }
                n2 = object.getProcessedData(arrby, 0);
                if (n2 <= 0) continue;
                audioFileWriter.writePacket(arrby, 0, n2);
            } while (true);
        }
        catch (EOFException var14_10) {
            audioFileWriter.close();
            dataInputStream.close();
            return;
        }
    }

    protected static int readInt(byte[] arrby, int n) {
        return arrby[n] & 255 | (arrby[n + 1] & 255) << 8 | (arrby[n + 2] & 255) << 16 | arrby[n + 3] << 24;
    }

    protected static int readShort(byte[] arrby, int n) {
        return arrby[n] & 255 | arrby[n + 1] << 8;
    }
}


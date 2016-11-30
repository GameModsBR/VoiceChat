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
import java.util.Random;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.OggCrc;
import org.xiph.speex.PcmWaveWriter;
import org.xiph.speex.RawWriter;
import org.xiph.speex.SbEncoder;
import org.xiph.speex.SpeexDecoder;

public class JSpeexDec {
    public static final String VERSION = "Java Speex Command Line Decoder v0.9.7 ($Revision: 1.4 $)";
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
    protected static Random random = new Random();
    protected SpeexDecoder speexDecoder;
    protected boolean enhanced = true;
    private int mode = 0;
    private int quality = 8;
    private int nframes = 1;
    private int sampleRate = -1;
    private float vbr_quality = -1.0f;
    private boolean vbr = false;
    private int channels = 1;
    private int loss = 0;
    protected String srcFile;
    protected String destFile;

    public static void main(String[] arrstring) throws IOException {
        JSpeexDec jSpeexDec = new JSpeexDec();
        if (jSpeexDec.parseArgs(arrstring)) {
            jSpeexDec.decode();
        }
    }

    public boolean parseArgs(String[] arrstring) {
        if (arrstring.length < 2) {
            if (arrstring.length == 1 && (arrstring[0].equals("-v") || arrstring[0].equals("--version"))) {
                JSpeexDec.version();
                return false;
            }
            JSpeexDec.usage();
            return false;
        }
        this.srcFile = arrstring[arrstring.length - 2];
        this.destFile = arrstring[arrstring.length - 1];
        this.srcFormat = this.srcFile.toLowerCase().endsWith(".spx") ? 1 : (this.srcFile.toLowerCase().endsWith(".wav") ? 2 : 0);
        this.destFormat = this.destFile.toLowerCase().endsWith(".wav") ? 2 : 0;
        for (int i = 0; i < arrstring.length - 2; ++i) {
            if (arrstring[i].equalsIgnoreCase("-h") || arrstring[i].equalsIgnoreCase("--help")) {
                JSpeexDec.usage();
                return false;
            }
            if (arrstring[i].equalsIgnoreCase("-v") || arrstring[i].equalsIgnoreCase("--version")) {
                JSpeexDec.version();
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
            if (arrstring[i].equalsIgnoreCase("--enh")) {
                this.enhanced = true;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--no-enh")) {
                this.enhanced = false;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--packet-loss")) {
                try {
                    this.loss = Integer.parseInt(arrstring[++i]);
                    continue;
                }
                catch (NumberFormatException var3_3) {
                    JSpeexDec.usage();
                    return false;
                }
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
                catch (NumberFormatException var3_4) {
                    JSpeexDec.usage();
                    return false;
                }
            }
            if (arrstring[i].equalsIgnoreCase("--nframes")) {
                try {
                    this.nframes = Integer.parseInt(arrstring[++i]);
                    continue;
                }
                catch (NumberFormatException var3_5) {
                    JSpeexDec.usage();
                    return false;
                }
            }
            if (arrstring[i].equalsIgnoreCase("--vbr")) {
                this.vbr = true;
                continue;
            }
            if (arrstring[i].equalsIgnoreCase("--stereo")) {
                this.channels = 2;
                continue;
            }
            JSpeexDec.usage();
            return false;
        }
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
        return true;
    }

    public static void usage() {
        JSpeexDec.version();
        System.out.println("Usage: JSpeexDec [options] input_file output_file");
        System.out.println("Where:");
        System.out.println("  input_file can be:");
        System.out.println("    filename.spx  an Ogg Speex file");
        System.out.println("    filename.wav  a Wave Speex file (beta!!!)");
        System.out.println("    filename.*    a raw Speex file");
        System.out.println("  output_file can be:");
        System.out.println("    filename.wav  a PCM wav file");
        System.out.println("    filename.*    a raw PCM file (any extension other than .wav)");
        System.out.println("Options: -h, --help     This help");
        System.out.println("         -v, --version    Version information");
        System.out.println("         --verbose        Print detailed information");
        System.out.println("         --quiet          Print minimal information");
        System.out.println("         --enh            Enable perceptual enhancement (default)");
        System.out.println("         --no-enh         Disable perceptual enhancement");
        System.out.println("         --packet-loss n  Simulate n % random packet loss");
        System.out.println("         if the input file is raw Speex (not Ogg Speex)");
        System.out.println("         -n, -nb          Narrowband (8kHz)");
        System.out.println("         -w, -wb          Wideband (16kHz)");
        System.out.println("         -u, -uwb         Ultra-Wideband (32kHz)");
        System.out.println("         --quality n      Encoding quality (0-10) default 8");
        System.out.println("         --nframes n      Number of frames per Ogg packet, default 1");
        System.out.println("         --vbr            Enable varible bit-rate (VBR)");
        System.out.println("         --stereo         Consider input as stereo");
        System.out.println("More information is available from: http://jspeex.sourceforge.net/");
        System.out.println("This code is a Java port of the Speex codec: http://www.speex.org/");
    }

    public static void version() {
        System.out.println("Java Speex Command Line Decoder v0.9.7 ($Revision: 1.4 $)");
        System.out.println("using Java Speex Decoder v0.9.7 ($Revision: 1.4 $)");
        System.out.println("Copyright (C) 2002-2004 Wimba S.A.");
    }

    public void decode() throws IOException {
        this.decode(new File(this.srcFile), new File(this.destFile));
    }

    public void decode(File file, File file2) throws IOException {
        byte[] arrby = new byte[2048];
        byte[] arrby2 = new byte[65536];
        byte[] arrby3 = new byte[176400];
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        if (this.printlevel <= 1) {
            JSpeexDec.version();
        }
        if (this.printlevel <= 0) {
            System.out.println("");
        }
        if (this.printlevel <= 0) {
            System.out.println("Input File: " + file);
        }
        this.speexDecoder = new SpeexDecoder();
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        AudioFileWriter audioFileWriter = null;
        try {
            do {
                int n6;
                if (this.srcFormat == 1) {
                    dataInputStream.readFully(arrby, 0, 27);
                    int n7 = JSpeexDec.readInt(arrby, 22);
                    arrby[22] = 0;
                    arrby[23] = 0;
                    arrby[24] = 0;
                    arrby[25] = 0;
                    int n8 = OggCrc.checksum(0, arrby, 0, 27);
                    if (!"OggS".equals(new String(arrby, 0, 4))) {
                        System.err.println("missing ogg id!");
                        return;
                    }
                    n = arrby[26] & 255;
                    dataInputStream.readFully(arrby, 27, n);
                    n8 = OggCrc.checksum(n8, arrby, 27, n);
                    for (n2 = 0; n2 < n; ++n2) {
                        n3 = arrby[27 + n2] & 255;
                        if (n3 == 255) {
                            System.err.println("sorry, don't handle 255 sizes!");
                            return;
                        }
                        dataInputStream.readFully(arrby2, 0, n3);
                        n8 = OggCrc.checksum(n8, arrby2, 0, n3);
                        if (n5 == 0) {
                            if (this.readSpeexHeader(arrby2, 0, n3)) {
                                if (this.printlevel <= 0) {
                                    System.out.println("File Format: Ogg Speex");
                                    System.out.println("Sample Rate: " + this.sampleRate);
                                    System.out.println("Channels: " + this.channels);
                                    System.out.println("Encoder mode: " + (this.mode == 0 ? "Narrowband" : (this.mode == 1 ? "Wideband" : "UltraWideband")));
                                    System.out.println("Frames per packet: " + this.nframes);
                                }
                                if (this.destFormat == 2) {
                                    audioFileWriter = new PcmWaveWriter(this.speexDecoder.getSampleRate(), this.speexDecoder.getChannels());
                                    if (this.printlevel <= 0) {
                                        System.out.println("");
                                        System.out.println("Output File: " + file2);
                                        System.out.println("File Format: PCM Wave");
                                        System.out.println("Perceptual Enhancement: " + this.enhanced);
                                    }
                                } else {
                                    audioFileWriter = new RawWriter();
                                    if (this.printlevel <= 0) {
                                        System.out.println("");
                                        System.out.println("Output File: " + file2);
                                        System.out.println("File Format: Raw Audio");
                                        System.out.println("Perceptual Enhancement: " + this.enhanced);
                                    }
                                }
                                audioFileWriter.open(file2);
                                audioFileWriter.writeHeader(null);
                                ++n5;
                                continue;
                            }
                            n5 = 0;
                            continue;
                        }
                        if (n5 == 1) {
                            ++n5;
                            continue;
                        }
                        if (this.loss > 0 && random.nextInt(100) < this.loss) {
                            this.speexDecoder.processData(null, 0, n3);
                            for (n6 = 1; n6 < this.nframes; ++n6) {
                                this.speexDecoder.processData(true);
                            }
                        } else {
                            this.speexDecoder.processData(arrby2, 0, n3);
                            for (n6 = 1; n6 < this.nframes; ++n6) {
                                this.speexDecoder.processData(false);
                            }
                        }
                        if ((n4 = this.speexDecoder.getProcessedData(arrby3, 0)) > 0) {
                            audioFileWriter.writePacket(arrby3, 0, n4);
                        }
                        ++n5;
                    }
                    if (n8 == n7) continue;
                    throw new IOException("Ogg CheckSums do not match");
                }
                if (n5 == 0) {
                    if (this.srcFormat == 2) {
                        dataInputStream.readFully(arrby, 0, 12);
                        if (!"RIFF".equals(new String(arrby, 0, 4)) && !"WAVE".equals(new String(arrby, 8, 4))) {
                            System.err.println("Not a WAVE file");
                            return;
                        }
                        dataInputStream.readFully(arrby, 0, 8);
                        String string = new String(arrby, 0, 4);
                        int n9 = JSpeexDec.readInt(arrby, 4);
                        while (!string.equals("data")) {
                            dataInputStream.readFully(arrby, 0, n9);
                            if (string.equals("fmt ")) {
                                if (JSpeexDec.readShort(arrby, 0) != -24311) {
                                    System.err.println("Not a Wave Speex file");
                                    return;
                                }
                                this.channels = JSpeexDec.readShort(arrby, 2);
                                this.sampleRate = JSpeexDec.readInt(arrby, 4);
                                n3 = JSpeexDec.readShort(arrby, 12);
                                if (JSpeexDec.readShort(arrby, 16) < 82) {
                                    System.err.println("Possibly corrupt Speex Wave file.");
                                    return;
                                }
                                this.readSpeexHeader(arrby, 20, 80);
                                if (this.printlevel <= 0) {
                                    System.out.println("File Format: Wave Speex");
                                    System.out.println("Sample Rate: " + this.sampleRate);
                                    System.out.println("Channels: " + this.channels);
                                    System.out.println("Encoder mode: " + (this.mode == 0 ? "Narrowband" : (this.mode == 1 ? "Wideband" : "UltraWideband")));
                                    System.out.println("Frames per packet: " + this.nframes);
                                }
                            }
                            dataInputStream.readFully(arrby, 0, 8);
                            string = new String(arrby, 0, 4);
                            n9 = JSpeexDec.readInt(arrby, 4);
                        }
                        if (this.printlevel <= 0) {
                            System.out.println("Data size: " + n9);
                        }
                    } else {
                        if (this.printlevel <= 0) {
                            System.out.println("File Format: Raw Speex");
                            System.out.println("Sample Rate: " + this.sampleRate);
                            System.out.println("Channels: " + this.channels);
                            System.out.println("Encoder mode: " + (this.mode == 0 ? "Narrowband" : (this.mode == 1 ? "Wideband" : "UltraWideband")));
                            System.out.println("Frames per packet: " + this.nframes);
                        }
                        this.speexDecoder.init(this.mode, this.sampleRate, this.channels, this.enhanced);
                        if (!this.vbr) {
                            switch (this.mode) {
                                case 0: {
                                    n3 = NbEncoder.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[this.quality]];
                                    break;
                                }
                                case 1: {
                                    n3 = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[this.quality]];
                                    n3 += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[this.quality]];
                                    break;
                                }
                                case 2: {
                                    n3 = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[this.quality]];
                                    n3 += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[this.quality]];
                                    n3 += SbEncoder.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[this.quality]];
                                    break;
                                }
                                default: {
                                    throw new IOException("Illegal mode encoundered.");
                                }
                            }
                            n3 = n3 + 7 >> 3;
                        } else {
                            n3 = 0;
                        }
                    }
                    if (this.destFormat == 2) {
                        audioFileWriter = new PcmWaveWriter(this.sampleRate, this.channels);
                        if (this.printlevel <= 0) {
                            System.out.println("");
                            System.out.println("Output File: " + file2);
                            System.out.println("File Format: PCM Wave");
                            System.out.println("Perceptual Enhancement: " + this.enhanced);
                        }
                    } else {
                        audioFileWriter = new RawWriter();
                        if (this.printlevel <= 0) {
                            System.out.println("");
                            System.out.println("Output File: " + file2);
                            System.out.println("File Format: Raw Audio");
                            System.out.println("Perceptual Enhancement: " + this.enhanced);
                        }
                    }
                    audioFileWriter.open(file2);
                    audioFileWriter.writeHeader(null);
                    ++n5;
                    continue;
                }
                dataInputStream.readFully(arrby2, 0, n3);
                if (this.loss > 0 && random.nextInt(100) < this.loss) {
                    this.speexDecoder.processData(null, 0, n3);
                    for (n6 = 1; n6 < this.nframes; ++n6) {
                        this.speexDecoder.processData(true);
                    }
                } else {
                    this.speexDecoder.processData(arrby2, 0, n3);
                    for (n6 = 1; n6 < this.nframes; ++n6) {
                        this.speexDecoder.processData(false);
                    }
                }
                if ((n4 = this.speexDecoder.getProcessedData(arrby3, 0)) > 0) {
                    audioFileWriter.writePacket(arrby3, 0, n4);
                }
                ++n5;
            } while (true);
        }
        catch (EOFException var24_17) {
            audioFileWriter.close();
            return;
        }
    }

    private boolean readSpeexHeader(byte[] arrby, int n, int n2) {
        if (n2 != 80) {
            System.out.println("Oooops");
            return false;
        }
        if (!"Speex   ".equals(new String(arrby, n, 8))) {
            return false;
        }
        this.mode = arrby[40 + n] & 255;
        this.sampleRate = JSpeexDec.readInt(arrby, n + 36);
        this.channels = JSpeexDec.readInt(arrby, n + 48);
        this.nframes = JSpeexDec.readInt(arrby, n + 64);
        return this.speexDecoder.init(this.mode, this.sampleRate, this.channels, this.enhanced);
    }

    protected static int readInt(byte[] arrby, int n) {
        return arrby[n] & 255 | (arrby[n + 1] & 255) << 8 | (arrby[n + 2] & 255) << 16 | arrby[n + 3] << 24;
    }

    protected static int readShort(byte[] arrby, int n) {
        return arrby[n] & 255 | arrby[n + 1] << 8;
    }
}


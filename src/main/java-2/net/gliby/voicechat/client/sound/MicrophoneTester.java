/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.client.sound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.Recorder;

public class MicrophoneTester
implements Runnable {
    private TargetDataLine line;
    private Thread thread;
    public boolean recording;
    private final VoiceChatClient voiceChat;
    public float currentAmplitude;

    public MicrophoneTester(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
    }

    private byte[] boostVolume(byte[] data) {
        int USHORT_MASK = 65535;
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer newBuf = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);
        while (buf.hasRemaining()) {
            int sample = buf.getShort() & 65535;
            newBuf.putShort((short)((sample *= 1 + (int)(this.voiceChat.getSettings().getInputBoost() * 5.0f)) & 65535));
        }
        return newBuf.array();
    }

    public Thread getThread() {
        return this.thread;
    }

    @Override
    public void run() {
        this.voiceChat.setRecorderActive(false);
        this.voiceChat.recorder.stop();
        this.line = this.voiceChat.getSettings().getInputDevice().getLine();
        if (this.line == null) {
            VoiceChatClient.getLogger().fatal("No line in found, cannot test input device.");
            return;
        }
        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, ClientStreamManager.universalAudioFormat);
        try {
            int numBytesRead;
            TargetDataLine targetLine = this.line;
            targetLine.open(ClientStreamManager.universalAudioFormat);
            targetLine.start();
            SourceDataLine sourceLine = (SourceDataLine)AudioSystem.getLine(sourceInfo);
            sourceLine.open(ClientStreamManager.universalAudioFormat);
            sourceLine.start();
            byte[] targetData = new byte[targetLine.getBufferSize() / 5];
            while (this.recording && (numBytesRead = targetLine.read(targetData, 0, targetData.length)) != -1) {
                byte[] boostedTargetData = this.boostVolume(targetData);
                sourceLine.write(boostedTargetData, 0, numBytesRead);
                double sum = 0.0;
                for (int i = 0; i < numBytesRead; ++i) {
                    sum += (double)(boostedTargetData[i] * boostedTargetData[i]);
                }
                if (numBytesRead <= 0) continue;
                this.currentAmplitude = (int)Math.sqrt(sum / (double)numBytesRead);
            }
            sourceLine.flush();
            sourceLine.close();
            this.line.flush();
            this.line.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.thread = new Thread((Runnable)this, "Input Device Tester");
        this.recording = true;
        this.thread.start();
    }

    public void stop() {
        this.recording = false;
        this.thread = null;
    }

    public void toggle() {
        if (this.recording) {
            this.start();
        } else {
            this.stop();
        }
    }
}


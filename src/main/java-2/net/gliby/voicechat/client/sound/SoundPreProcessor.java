/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package net.gliby.voicechat.client.sound;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.debug.Statistics;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.minecraft.client.Minecraft;
import org.xiph.speex.SpeexDecoder;

public class SoundPreProcessor {
    VoiceChatClient voiceChat;
    Statistics stats;
    SpeexDecoder decoder;
    byte[] buffer;

    public static List<byte[]> divideArray(byte[] source, int chunksize) {
        ArrayList<byte[]> result = new ArrayList<byte[]>();
        for (int start = 0; start < source.length; start += chunksize) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
        }
        return result;
    }

    public SoundPreProcessor(VoiceChatClient voiceChat, Minecraft mc) {
        this.voiceChat = voiceChat;
        this.stats = VoiceChatClient.getStatistics();
    }

    public boolean process(int id, byte[] encodedSamples, int chunkSize, boolean direct) {
        if (chunkSize > encodedSamples.length) {
            VoiceChatClient.getLogger().fatal("Sound Pre-Processor has been given incorrect data from network, sample pieces cannot be bigger than whole sample. ");
            return false;
        }
        if (this.decoder == null) {
            this.decoder = new SpeexDecoder();
            this.decoder.init(0, (int)ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
        }
        byte[] decodedData = null;
        if (encodedSamples.length <= chunkSize) {
            try {
                this.decoder.processData(encodedSamples, 0, encodedSamples.length);
            }
            catch (StreamCorruptedException e) {
                e.printStackTrace();
                return false;
            }
            decodedData = new byte[this.decoder.getProcessedDataByteSize()];
            this.decoder.getProcessedData(decodedData, 0);
        } else {
            List<byte[]> samplesList = SoundPreProcessor.divideArray(encodedSamples, chunkSize);
            this.buffer = new byte[0];
            for (int i = 0; i < samplesList.size(); ++i) {
                byte[] sample = samplesList.get(i);
                SpeexDecoder tempDecoder = new SpeexDecoder();
                tempDecoder.init(0, (int)ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
                try {
                    this.decoder.processData(sample, 0, sample.length);
                }
                catch (StreamCorruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                byte[] sampleBuffer = new byte[this.decoder.getProcessedDataByteSize()];
                this.decoder.getProcessedData(sampleBuffer, 0);
                this.write(sampleBuffer);
            }
            decodedData = this.buffer;
        }
        if (decodedData != null) {
            VoiceChatClient.getSoundManager().addQueue(decodedData, direct, id);
            if (this.stats != null) {
                this.stats.addEncodedSamples(encodedSamples.length);
                this.stats.addDecodedSamples(decodedData.length);
            }
            this.buffer = new byte[0];
            return true;
        }
        return false;
    }

    private void write(byte[] write) {
        byte[] result = new byte[this.buffer.length + write.length];
        System.arraycopy(this.buffer, 0, result, 0, this.buffer.length);
        System.arraycopy(write, 0, result, this.buffer.length, write.length);
        this.buffer = result;
    }
}


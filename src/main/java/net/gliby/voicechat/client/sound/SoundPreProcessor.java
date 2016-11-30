package net.gliby.voicechat.client.sound;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


   public static List divideArray(byte[] source, int chunksize) {
      ArrayList result = new ArrayList();

      for(int start = 0; start < source.length; start += chunksize) {
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
      if(chunkSize > encodedSamples.length) {
         VoiceChatClient.getLogger().fatal("Sound Pre-Processor has been given incorrect data from network, sample pieces cannot be bigger than whole sample. ");
         return false;
      } else {
         if(this.decoder == null) {
            this.decoder = new SpeexDecoder();
            this.decoder.init(0, (int)ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
         }

         Object decodedData = null;
         byte[] var13;
         if(encodedSamples.length <= chunkSize) {
            try {
               this.decoder.processData(encodedSamples, 0, encodedSamples.length);
            } catch (StreamCorruptedException var12) {
               var12.printStackTrace();
               return false;
            }

            var13 = new byte[this.decoder.getProcessedDataByteSize()];
            this.decoder.getProcessedData(var13, 0);
         } else {
            List samplesList = divideArray(encodedSamples, chunkSize);
            this.buffer = new byte[0];

            for(int i = 0; i < samplesList.size(); ++i) {
               byte[] sample = samplesList.get(i);
               SpeexDecoder tempDecoder = new SpeexDecoder();
               tempDecoder.init(0, (int)ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());

               try {
                  this.decoder.processData(sample, 0, sample.length);
               } catch (StreamCorruptedException var11) {
                  var11.printStackTrace();
                  return false;
               }

               byte[] sampleBuffer = new byte[this.decoder.getProcessedDataByteSize()];
               this.decoder.getProcessedData(sampleBuffer, 0);
               this.write(sampleBuffer);
            }

            var13 = this.buffer;
         }

         if(var13 != null) {
            VoiceChatClient.getSoundManager().addQueue(var13, direct, id);
            if(this.stats != null) {
               this.stats.addEncodedSamples(encodedSamples.length);
               this.stats.addDecodedSamples(var13.length);
            }

            this.buffer = new byte[0];
            return true;
         } else {
            return false;
         }
      }
   }

   private void write(byte[] write) {
      byte[] result = new byte[this.buffer.length + write.length];
      System.arraycopy(this.buffer, 0, result, 0, this.buffer.length);
      System.arraycopy(write, 0, result, this.buffer.length, write.length);
      this.buffer = result;
   }
}

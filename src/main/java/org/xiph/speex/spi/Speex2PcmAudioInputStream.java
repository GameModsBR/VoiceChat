package org.xiph.speex.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import javax.sound.sampled.AudioFormat;
import org.xiph.speex.Bits;
import org.xiph.speex.Decoder;
import org.xiph.speex.NbDecoder;
import org.xiph.speex.SbDecoder;
import org.xiph.speex.spi.FilteredAudioInputStream;

public class Speex2PcmAudioInputStream extends FilteredAudioInputStream {

   private boolean initialised;
   private int sampleRate;
   private int channelCount;
   private float[] decodedData;
   private byte[] outputData;
   private Bits bits;
   private Decoder decoder;
   private int frameSize;
   private int framesPerPacket;
   private int streamSerialNumber;
   private int packetsPerOggPage;
   private int packetCount;
   private byte[] packetSizes;


   public Speex2PcmAudioInputStream(InputStream var1, AudioFormat var2, long var3) {
      this(var1, var2, var3, 2048);
   }

   public Speex2PcmAudioInputStream(InputStream var1, AudioFormat var2, long var3, int var5) {
      super(var1, var2, var3, var5);
      this.bits = new Bits();
      this.packetSizes = new byte[256];
      this.initialised = false;
   }

   protected void initialise(boolean var1) throws IOException {
      while(!this.initialised) {
         int var2 = this.prebuf.length - this.precount - 1;
         int var3 = this.in.available();
         if(!var1 && var3 <= 0) {
            return;
         }

         var2 = var3 > 0?Math.min(var3, var2):var2;
         int var4 = this.in.read(this.prebuf, this.precount, var2);
         if(var4 < 0) {
            throw new StreamCorruptedException("Incomplete Ogg Headers");
         }

         if(var4 == 0) {
            ;
         }

         this.precount += var4;
         int var5;
         if(this.decoder == null && this.precount >= 108) {
            if(!(new String(this.prebuf, 0, 4)).equals("OggS")) {
               throw new StreamCorruptedException("The given stream does not appear to be Ogg.");
            }

            this.streamSerialNumber = readInt(this.prebuf, 14);
            if(!(new String(this.prebuf, 28, 8)).equals("Speex   ")) {
               throw new StreamCorruptedException("The given stream does not appear to be Ogg Speex.");
            }

            this.sampleRate = readInt(this.prebuf, 64);
            this.channelCount = readInt(this.prebuf, 76);
            this.framesPerPacket = readInt(this.prebuf, 92);
            var5 = readInt(this.prebuf, 68);
            switch(var5) {
            case 0:
               this.decoder = new NbDecoder();
               ((NbDecoder)this.decoder).nbinit();
               break;
            case 1:
               this.decoder = new SbDecoder();
               ((SbDecoder)this.decoder).wbinit();
               break;
            case 2:
               this.decoder = new SbDecoder();
               ((SbDecoder)this.decoder).uwbinit();
            }

            this.decoder.setPerceptualEnhancement(true);
            this.frameSize = this.decoder.getFrameSize();
            this.decodedData = new float[this.frameSize * this.channelCount];
            this.outputData = new byte[2 * this.frameSize * this.channelCount * this.framesPerPacket];
            this.bits.init();
         }

         if(this.decoder != null && this.precount >= 135) {
            this.packetsPerOggPage = 255 & this.prebuf[134];
            if(this.precount >= 135 + this.packetsPerOggPage) {
               var5 = 0;

               for(int var6 = 0; var6 < this.packetsPerOggPage; ++var6) {
                  var5 += 255 & this.prebuf[135 + var6];
               }

               if(this.precount >= 135 + this.packetsPerOggPage + var5) {
                  this.prepos = 135 + this.packetsPerOggPage + var5;
                  this.packetsPerOggPage = 0;
                  this.packetCount = 255;
                  this.initialised = true;
               }
            }
         }
      }

   }

   protected void fill() throws IOException {
      this.makeSpace();

      while(!this.initialised) {
         this.initialise(true);
      }

      while(true) {
         int var1 = this.in.read(this.prebuf, this.precount, this.prebuf.length - this.precount);
         byte var2;
         int var3;
         byte[] var4;
         if(var1 < 0) {
            while(this.prepos < this.precount) {
               if(this.packetCount >= this.packetsPerOggPage) {
                  this.readOggPageHeader();
               }

               if(this.packetCount < this.packetsPerOggPage) {
                  var2 = this.packetSizes[this.packetCount++];
                  if(this.precount - this.prepos < var2) {
                     throw new StreamCorruptedException("Incompleted last Speex packet");
                  }

                  this.decode(this.prebuf, this.prepos, var2);

                  for(this.prepos += var2; this.buf.length - this.count < this.outputData.length; this.buf = var4) {
                     var3 = this.buf.length * 2;
                     var4 = new byte[var3];
                     System.arraycopy(this.buf, 0, var4, 0, this.count);
                  }

                  System.arraycopy(this.outputData, 0, this.buf, this.count, this.outputData.length);
                  this.count += this.outputData.length;
               }
            }

            return;
         }

         if(var1 >= 0) {
            this.precount += var1;
            if(this.packetCount >= this.packetsPerOggPage) {
               this.readOggPageHeader();
            }

            if(this.packetCount < this.packetsPerOggPage && this.precount - this.prepos >= this.packetSizes[this.packetCount]) {
               while(this.precount - this.prepos >= this.packetSizes[this.packetCount] && this.packetCount < this.packetsPerOggPage) {
                  var2 = this.packetSizes[this.packetCount++];
                  this.decode(this.prebuf, this.prepos, var2);

                  for(this.prepos += var2; this.buf.length - this.count < this.outputData.length; this.buf = var4) {
                     var3 = this.buf.length * 2;
                     var4 = new byte[var3];
                     System.arraycopy(this.buf, 0, var4, 0, this.count);
                  }

                  System.arraycopy(this.outputData, 0, this.buf, this.count, this.outputData.length);
                  this.count += this.outputData.length;
                  if(this.packetCount >= this.packetsPerOggPage) {
                     this.readOggPageHeader();
                  }
               }

               System.arraycopy(this.prebuf, this.prepos, this.prebuf, 0, this.precount - this.prepos);
               this.precount -= this.prepos;
               this.prepos = 0;
               return;
            }
         }
      }
   }

   protected void decode(byte[] var1, int var2, int var3) throws StreamCorruptedException {
      int var6 = 0;
      this.bits.read_from(var1, var2, var3);

      for(int var7 = 0; var7 < this.framesPerPacket; ++var7) {
         this.decoder.decode(this.bits, this.decodedData);
         if(this.channelCount == 2) {
            this.decoder.decodeStereo(this.decodedData, this.frameSize);
         }

         int var4;
         for(var4 = 0; var4 < this.frameSize * this.channelCount; ++var4) {
            if(this.decodedData[var4] > 32767.0F) {
               this.decodedData[var4] = 32767.0F;
            } else if(this.decodedData[var4] < -32768.0F) {
               this.decodedData[var4] = -32768.0F;
            }
         }

         for(var4 = 0; var4 < this.frameSize * this.channelCount; ++var4) {
            short var5 = this.decodedData[var4] > 0.0F?(short)((int)((double)this.decodedData[var4] + 0.5D)):(short)((int)((double)this.decodedData[var4] - 0.5D));
            this.outputData[var6++] = (byte)(var5 & 255);
            this.outputData[var6++] = (byte)(var5 >> 8 & 255);
         }
      }

   }

   public synchronized long skip(long var1) throws IOException {
      while(!this.initialised) {
         this.initialise(true);
      }

      this.checkIfStillOpen();
      if(var1 <= 0L) {
         return 0L;
      } else if(this.pos < this.count) {
         return super.skip(var1);
      } else {
         int var3 = 2 * this.framesPerPacket * this.frameSize * this.channelCount;
         if(this.markpos < 0 && var1 >= (long)var3) {
            if(this.packetCount >= this.packetsPerOggPage) {
               this.readOggPageHeader();
            }

            if(this.packetCount < this.packetsPerOggPage) {
               int var4 = 0;
               if(this.precount - this.prepos < this.packetSizes[this.packetCount]) {
                  int var5 = this.in.available();
                  if(var5 > 0) {
                     int var6 = Math.min(this.prebuf.length - this.precount, var5);
                     int var7 = this.in.read(this.prebuf, this.precount, var6);
                     if(var7 < 0) {
                        throw new IOException("End of stream but there are still supposed to be packets to decode");
                     }

                     this.precount += var7;
                  }
               }

               while(this.precount - this.prepos >= this.packetSizes[this.packetCount] && this.packetCount < this.packetsPerOggPage && var1 >= (long)var3) {
                  this.prepos += this.packetSizes[this.packetCount++];
                  var4 += var3;
                  var1 -= (long)var3;
                  if(this.packetCount >= this.packetsPerOggPage) {
                     this.readOggPageHeader();
                  }
               }

               System.arraycopy(this.prebuf, this.prepos, this.prebuf, 0, this.precount - this.prepos);
               this.precount -= this.prepos;
               this.prepos = 0;
               return (long)var4;
            }
         }

         return super.skip(var1);
      }
   }

   public synchronized int available() throws IOException {
      if(!this.initialised) {
         this.initialise(false);
         if(!this.initialised) {
            return 0;
         }
      }

      int var1 = super.available();
      if(this.packetCount >= this.packetsPerOggPage) {
         this.readOggPageHeader();
      }

      if(this.packetCount < this.packetsPerOggPage) {
         int var2 = this.precount - this.prepos + this.in.available();
         byte var3 = this.packetSizes[this.packetCount];

         for(int var4 = 0; var3 < var2 && this.packetCount + var4 < this.packetsPerOggPage; var3 = this.packetSizes[this.packetCount + var4]) {
            var2 -= var3;
            var1 += 2 * this.frameSize * this.framesPerPacket;
            ++var4;
         }
      }

      return var1;
   }

   private void readOggPageHeader() throws IOException {
      int var1 = 0;
      int var2;
      int var3;
      int var4;
      if(this.precount - this.prepos < 27) {
         var2 = this.in.available();
         if(var2 > 0) {
            var3 = Math.min(this.prebuf.length - this.precount, var2);
            var4 = this.in.read(this.prebuf, this.precount, var3);
            if(var4 < 0) {
               throw new IOException("End of stream but available was positive");
            }

            this.precount += var4;
         }
      }

      if(this.precount - this.prepos >= 27) {
         if(!(new String(this.prebuf, this.prepos, 4)).equals("OggS")) {
            throw new StreamCorruptedException("Lost Ogg Sync");
         }

         if(this.streamSerialNumber != readInt(this.prebuf, this.prepos + 14)) {
            throw new StreamCorruptedException("Ogg Stream Serial Number mismatch");
         }

         var1 = 255 & this.prebuf[this.prepos + 26];
      }

      if(this.precount - this.prepos < 27 + var1) {
         var2 = this.in.available();
         if(var2 > 0) {
            var3 = Math.min(this.prebuf.length - this.precount, var2);
            var4 = this.in.read(this.prebuf, this.precount, var3);
            if(var4 < 0) {
               throw new IOException("End of stream but available was positive");
            }

            this.precount += var4;
         }
      }

      if(this.precount - this.prepos >= 27 + var1) {
         System.arraycopy(this.prebuf, this.prepos + 27, this.packetSizes, 0, var1);
         this.packetCount = 0;
         this.prepos += 27 + var1;
         this.packetsPerOggPage = var1;
      }

   }

   private static int readInt(byte[] var0, int var1) {
      return var0[var1] & 255 | (var0[var1 + 1] & 255) << 8 | (var0[var1 + 2] & 255) << 16 | var0[var1 + 3] << 24;
   }
}

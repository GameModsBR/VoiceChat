package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPByteUtilities;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPClientVoicePacket extends UDPPacket {

   byte[] samples;
   byte divider;


   public UDPClientVoicePacket(byte divider, byte[] samples) {
      this.samples = samples;
      this.divider = divider;
   }

   public byte id() {
      return (byte)1;
   }

   public void write(ByteArrayDataOutput out) {
      UDPByteUtilities.writeBytes(this.samples, out);
      out.writeByte(this.divider);
   }
}

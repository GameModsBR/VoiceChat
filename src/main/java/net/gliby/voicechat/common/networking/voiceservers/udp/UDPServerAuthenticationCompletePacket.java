package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPServerAuthenticationCompletePacket extends UDPPacket {

   public byte id() {
      return (byte)0;
   }

   public void write(ByteArrayDataOutput out) {}
}

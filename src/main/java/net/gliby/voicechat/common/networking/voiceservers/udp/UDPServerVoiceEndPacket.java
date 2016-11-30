package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPServerVoiceEndPacket extends UDPPacket {

   int entityID;


   public UDPServerVoiceEndPacket(int entityID) {
      this.entityID = entityID;
   }

   public byte id() {
      return (byte)2;
   }

   public void write(ByteArrayDataOutput out) {
      out.writeInt(this.entityID);
   }
}

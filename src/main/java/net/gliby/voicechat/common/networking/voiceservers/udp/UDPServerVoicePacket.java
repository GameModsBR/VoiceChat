package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPServerVoicePacket extends UDPPacket {

   public int entityID;
   public boolean direct;
   public byte[] data;


   public UDPServerVoicePacket(byte[] data, int entityId, boolean global) {
      this.data = data;
      this.entityID = entityId;
      this.direct = global;
   }

   public byte id() {
      return (byte)1;
   }

   public void write(ByteArrayDataOutput in) {
      in.writeInt(this.entityID);
      in.writeBoolean(this.direct);
      UDPByteUtilities.writeBytes(this.data, in);
   }
}

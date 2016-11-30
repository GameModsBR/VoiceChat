package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;
import net.gliby.voicechat.common.networking.voiceservers.VoiceAuthenticatedServer;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPClient;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPServerChunkVoicePacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPServerEntityPositionPacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPServerVoiceEndPacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPServerVoicePacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPVoiceServerHandler;
import net.gliby.voicechat.common.networking.voiceservers.udp.UdpServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class UDPVoiceServer extends VoiceAuthenticatedServer {

   public static volatile boolean running;
   private final VoiceChatServer voiceChat;
   private final ServerStreamManager manager;
   private UDPVoiceServerHandler handler;
   public Map clientMap;
   private UdpServer server;


   public UDPVoiceServer(VoiceChatServer voiceChat) {
      this.voiceChat = voiceChat;
      this.manager = voiceChat.getServerNetwork().getDataManager();
   }

   public void closeConnection(int id) {
      UDPClient client = (UDPClient)this.clientMap.get(id);
      if(client != null) {
         this.handler.closeConnection(client.socketAddress);
      }

      this.clientMap.remove(id);
   }

   public EnumVoiceNetworkType getType() {
      return EnumVoiceNetworkType.UDP;
   }

   public void handleVoiceData(EntityPlayerMP player, byte[] data, byte divider, int id, boolean end) {
      this.manager.addQueue(player, data, divider, id, end);
   }

   public void sendChunkVoiceData(EntityPlayerMP player, int entityID, boolean direct, byte[] samples, byte chunkSize) {
      UDPClient client = (UDPClient)this.clientMap.get(player.getEntityId());
      if(client != null) {
         this.sendPacket(new UDPServerChunkVoicePacket(samples, entityID, direct, chunkSize), client);
      }

   }

   public void sendEntityPosition(EntityPlayerMP player, int entityID, double x, double y, double z) {
      UDPClient client = (UDPClient)this.clientMap.get(player.getEntityId());
      if(client != null) {
         this.sendPacket(new UDPServerEntityPositionPacket(entityID, x, y, z), client);
      }

   }

   public void sendPacket(UDPPacket packet, UDPClient client) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeByte(packet.id());
      packet.write(out);
      byte[] data = out.toByteArray();

      try {
         this.server.send(new DatagramPacket(data, data.length, client.socketAddress));
      } catch (SocketException var6) {
         var6.printStackTrace();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

   }

   public void sendVoiceData(EntityPlayerMP player, int entityID, boolean global, byte[] samples) {
      UDPClient client = (UDPClient)this.clientMap.get(player.getEntityId());
      if(client != null) {
         this.sendPacket(new UDPServerVoicePacket(samples, entityID, global), client);
      }

   }

   public void sendVoiceEnd(EntityPlayerMP player, int entityID) {
      UDPClient client = (UDPClient)this.clientMap.get(player.getEntityId());
      if(client != null) {
         this.sendPacket(new UDPServerVoiceEndPacket(entityID), client);
      }

   }

   public boolean start() {
      this.clientMap = new HashMap();
      this.handler = new UDPVoiceServerHandler(this);
      String hostname = "0.0.0.0";
      MinecraftServer mc = MinecraftServer.getServer();
      if(mc.isDedicatedServer()) {
         hostname = mc.getServerHostname();
      }

      this.server = new UdpServer(VoiceChatServer.getLogger(), hostname, this.voiceChat.getServerSettings().getUDPPort());
      this.server.addUdpServerListener(new UdpServer.Listener() {
         public void packetReceived(UdpServer.Event evt) {
            try {
               UDPVoiceServer.this.handler.read(evt.getPacketAsBytes(), evt.getPacket());
            } catch (Exception var3) {
               var3.printStackTrace();
            }

         }
      });
      this.server.start();
      return true;
   }

   public void stop() {
      running = false;
      this.handler.close();
      this.server.clearUdpListeners();
      this.server.stop();
      this.clientMap.clear();
      this.handler = null;
      this.server = null;
   }
}

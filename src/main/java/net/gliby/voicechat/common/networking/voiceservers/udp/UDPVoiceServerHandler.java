package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.MathUtility;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPByteUtilities;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPClient;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPServerAuthenticationCompletePacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPVoiceServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class UDPVoiceServerHandler {

   private final ExecutorService threadService;
   private final Map clientNetworkMap;
   private final UDPVoiceServer server;


   public UDPVoiceServerHandler(UDPVoiceServer server) {
      this.server = server;
      this.threadService = Executors.newFixedThreadPool((int)MathUtility.clamp((float)MinecraftServer.getServer().getMaxPlayers(), 1.0F, 10.0F));
      this.clientNetworkMap = new HashMap();
   }

   public void close() {
      this.clientNetworkMap.clear();
      this.threadService.shutdown();
   }

   public void closeConnection(InetSocketAddress address) {
      this.clientNetworkMap.remove(address);
   }

   private void handleAuthetication(InetSocketAddress address, DatagramPacket packet, ByteArrayDataInput in) {
      String hash = null;

      try {
         hash = new String(UDPByteUtilities.readBytes(in), "UTF-8");
      } catch (UnsupportedEncodingException var7) {
         var7.printStackTrace();
         return;
      }

      EntityPlayerMP player = (EntityPlayerMP)this.server.waitingAuth.get(hash);
      if(player != null) {
         UDPClient client = new UDPClient(player, address, hash);
         this.clientNetworkMap.put(client.socketAddress, client);
         this.server.clientMap.put(Integer.valueOf(player.getEntityId()), client);
         this.server.waitingAuth.remove(hash);
         VoiceChat.getLogger().info(client + " has been authenticated by server.");
         this.server.sendPacket(new UDPServerAuthenticationCompletePacket(), client);
      }

   }

   private void handleVoice(UDPClient client, ByteArrayDataInput in) {
      this.server.handleVoiceData(client.player, UDPByteUtilities.readBytes(in), in.readByte(), client.player.getEntityId(), false);
   }

   private void handleVoiceEnd(UDPClient client) {
      this.server.handleVoiceData(client.player, (byte[])null, (byte)0, client.player.getEntityId(), true);
   }

   public void read(byte[] data, final DatagramPacket packet) throws Exception {
      final InetSocketAddress address = (InetSocketAddress)packet.getSocketAddress();
      final UDPClient client = (UDPClient)this.clientNetworkMap.get(address);
      final ByteArrayDataInput in = ByteStreams.newDataInput(data);
      final byte id = in.readByte();
      this.threadService.execute(new Runnable() {
         public void run() {
            if(id == 0) {
               UDPVoiceServerHandler.this.handleAuthetication(address, packet, in);
            }

            if(client != null) {
               switch(id) {
               case 1:
                  UDPVoiceServerHandler.this.handleVoice(client, in);
                  break;
               case 2:
                  UDPVoiceServerHandler.this.handleVoiceEnd(client);
               }
            }

         }
      });
   }
}

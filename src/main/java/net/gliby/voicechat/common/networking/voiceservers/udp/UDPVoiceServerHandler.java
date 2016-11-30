package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.MathUtility;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPVoiceServerHandler {

    private final ExecutorService threadService;
    private final Map<InetSocketAddress, UDPClient> clientNetworkMap;
    private final UDPVoiceServer server;


    public UDPVoiceServerHandler(UDPVoiceServer server) {
        this.server = server;
        this.threadService = Executors.newFixedThreadPool((int) MathUtility.clamp((float) server.getMinecraftServer().getMaxPlayers(), 1.0F, 10.0F));
        this.clientNetworkMap = new HashMap<InetSocketAddress, UDPClient>();
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

        EntityPlayerMP player = this.server.waitingAuth.get(hash);
        if (player != null) {
            UDPClient client = new UDPClient(player, address, hash);
            this.clientNetworkMap.put(client.socketAddress, client);
            this.server.clientMap.put(player.getEntityId(), client);
            this.server.waitingAuth.remove(hash);
            VoiceChat.getLogger().info(client + " has been authenticated by server.");
            this.server.sendPacket(new UDPServerAuthenticationCompletePacket(), client);
        }

    }

    private void handleVoice(UDPClient client, ByteArrayDataInput in) {
        this.server.handleVoiceData(client.player, UDPByteUtilities.readBytes(in), in.readByte(), client.player.getEntityId(), false);
    }

    private void handleVoiceEnd(UDPClient client) {
        this.server.handleVoiceData(client.player, null, (byte) 0, client.player.getEntityId(), true);
    }

    public void read(byte[] data, final DatagramPacket packet) throws Exception {
        final InetSocketAddress address = (InetSocketAddress) packet.getSocketAddress();
        final UDPClient client = this.clientNetworkMap.get(address);
        final ByteArrayDataInput in = ByteStreams.newDataInput(data);
        final byte id = in.readByte();
        this.threadService.execute(new Runnable() {
            @Override
            public void run() {
                if (id == 0) {
                    UDPVoiceServerHandler.this.handleAuthetication(address, packet, in);
                }

                if (client != null) {
                    switch (id) {
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

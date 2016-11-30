/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.management.ServerConfigurationManager
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessage
 */
package net.gliby.voicechat.common.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.ServerSettings;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.gliby.voicechat.common.networking.packets.MinecraftClientEntityDataPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ServerNetwork {
    private final VoiceChatServer voiceChat;
    private String externalAddress;
    public final ServerStreamManager dataManager;

    public ServerNetwork(VoiceChatServer voiceChat) {
        this.voiceChat = voiceChat;
        this.dataManager = new ServerStreamManager(voiceChat);
    }

    public String getAddress() {
        return this.externalAddress;
    }

    public ServerStreamManager getDataManager() {
        return this.dataManager;
    }

    public String[] getPlayerIPs() {
        List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        String[] ips = new String[players.size()];
        for (int i = 0; i < players.size(); ++i) {
            EntityPlayerMP p = (EntityPlayerMP)players.get(i);
            ips[i] = p.getPlayerIP();
        }
        return ips;
    }

    public EntityPlayerMP[] getPlayers() {
        List<EntityPlayerMP> pl = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        EntityPlayerMP[] players = pl.toArray(new EntityPlayerMP[pl.size()]);
        return players;
    }

    public void init() {
        if (this.voiceChat.getServerSettings().isUsingProxy()) {
            new Thread(new Runnable(){

                @Override
                public void run() {
                    ServerNetwork.this.externalAddress = ServerNetwork.this.retrieveExternalAddress();
                }
            }, "Extrernal Address Retriver Process").start();
        }
        this.dataManager.init();
    }

    private String retrieveExternalAddress() {
        VoiceChat.getLogger().info("Retrieving server address.");
        BufferedReader in = null;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            return in.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "0.0.0.0";
        }
    }

    public void sendEntityData(EntityPlayerMP player, int entityID, String username, double x, double y, double z) {
        VoiceChat.getDispatcher().sendTo((IMessage)new MinecraftClientEntityDataPacket(entityID, username, x, y, z), player);
    }

    public void stop() {
        this.dataManager.reset();
    }

}


/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraftforge.fml.common.FMLCommonHandler
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.PlayerEvent
 *  net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessage
 *  net.minecraftforge.fml.relauncher.Side
 *  org.apache.commons.lang3.RandomStringUtils
 */
package net.gliby.voicechat.common.networking.voiceservers;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.packets.MinecraftClientVoiceAuthenticatedServer;
import net.gliby.voicechat.common.networking.packets.MinecraftClientVoiceServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerConnectionHandler {
    VoiceChatServer voiceChat;

    public ServerConnectionHandler(VoiceChatServer vc) {
        this.voiceChat = vc;
    }

    @SubscribeEvent
    public void onConnected(final PlayerEvent.PlayerLoggedInEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(new Runnable(){

                @Override
                public void run() {
                    EntityPlayerMP player = (EntityPlayerMP)event.player;
                    if (ServerConnectionHandler.this.voiceChat.getVoiceServer() instanceof VoiceAuthenticatedServer) {
                        VoiceAuthenticatedServer voiceServer = (VoiceAuthenticatedServer)ServerConnectionHandler.this.voiceChat.getVoiceServer();
                        String hash = null;
                        while (hash == null) {
                            try {
                                hash = ServerConnectionHandler.this.sha256(RandomStringUtils.random(32));
                            }
                            catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                        voiceServer.waitingAuth.put(hash, player);
                        VoiceChat.getDispatcher().sendTo(new MinecraftClientVoiceAuthenticatedServer(ServerConnectionHandler.this.voiceChat.getServerSettings().canShowVoicePlates(), ServerConnectionHandler.this.voiceChat.getServerSettings().canShowVoiceIcons(), ServerConnectionHandler.this.voiceChat.getServerSettings().getMinimumSoundQuality(), ServerConnectionHandler.this.voiceChat.getServerSettings().getMaximumSoundQuality(), ServerConnectionHandler.this.voiceChat.getServerSettings().getBufferSize(), ServerConnectionHandler.this.voiceChat.getServerSettings().getSoundDistance(), ServerConnectionHandler.this.voiceChat.getVoiceServer().getType().ordinal(), ServerConnectionHandler.this.voiceChat.getServerSettings().getUDPPort(), hash, ServerConnectionHandler.this.voiceChat.serverSettings.isUsingProxy() ? ServerConnectionHandler.this.voiceChat.serverNetwork.getAddress() : ""), player);
                    } else {
                        VoiceChat.getDispatcher().sendTo(new MinecraftClientVoiceServerPacket(ServerConnectionHandler.this.voiceChat.getServerSettings().canShowVoicePlates(), ServerConnectionHandler.this.voiceChat.getServerSettings().canShowVoiceIcons(), ServerConnectionHandler.this.voiceChat.getServerSettings().getMinimumSoundQuality(), ServerConnectionHandler.this.voiceChat.getServerSettings().getMaximumSoundQuality(), ServerConnectionHandler.this.voiceChat.getServerSettings().getBufferSize(), ServerConnectionHandler.this.voiceChat.getServerSettings().getSoundDistance(), ServerConnectionHandler.this.voiceChat.getVoiceServer().getType().ordinal()), player);
                    }
                    ServerConnectionHandler.this.voiceChat.serverNetwork.dataManager.entityHandler.connected(player);
                }
            }, 500, TimeUnit.MILLISECONDS);
        }
    }

    @SubscribeEvent
    public void onDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            this.voiceChat.serverNetwork.dataManager.entityHandler.disconnected(event.player.getEntityId());
        }
    }

    private String sha256(String s) throws NoSuchAlgorithmException {
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(s.getBytes());
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                sb.append(0);
                sb.append(hex.charAt(hex.length() - 1));
                continue;
            }
            sb.append(hex.substring(hex.length() - 2));
        }
        return sb.toString();
    }

}


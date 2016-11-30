/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.management.ServerConfigurationManager
 *  net.minecraft.world.World
 *  net.minecraftforge.fml.common.eventhandler.Event
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 */
package net.gliby.voicechat.common.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.gliby.voicechat.common.ServerSettings;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.api.VoiceChatAPI;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ThreadDataQueue;
import net.gliby.voicechat.common.networking.ThreadDataUpdateStream;
import net.gliby.voicechat.common.networking.entityhandler.EntityHandler;
import net.gliby.voicechat.common.networking.voiceservers.VoiceServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;

public class ServerStreamManager {
    List<ServerStream> currentStreams;
    ConcurrentLinkedQueue<ServerDatalet> dataQueue;
    public ConcurrentHashMap<Integer, ServerStream> streaming;
    public HashMap<UUID, Integer> chatModeMap;
    private HashMap<Integer, List<Integer>> receivedEntityData;
    private Thread threadUpdate;
    private Thread treadQueue;
    private final VoiceChatServer voiceChat;
    public List<UUID> mutedPlayers;
    public EntityHandler entityHandler;
    volatile boolean running;

    ServerStreamManager(VoiceChatServer voiceChat) {
        this.voiceChat = voiceChat;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addQueue(EntityPlayerMP player, byte[] decoded_data, byte divider, int id, boolean end) {
        if (this.mutedPlayers.contains(player.getPersistentID())) {
            return;
        }
        this.dataQueue.offer(new ServerDatalet(player, id, decoded_data, divider, end));
        Thread thread = this.treadQueue;
        synchronized (thread) {
            this.treadQueue.notify();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addStreamSafe(ServerStream stream) {
        this.streaming.put(stream.id, stream);
        this.currentStreams.add(stream);
        Thread thread = this.threadUpdate;
        synchronized (thread) {
            this.threadUpdate.notify();
        }
    }

    public void createStream(ServerDatalet data) {
        ServerStream stream = new ServerStream(data.player, data.id, this.generateSource(data));
        this.addStreamSafe(stream);
        VoiceChatAPI.instance().bus().post((Event)new ServerStreamEvent.StreamCreated(this, stream, data));
        this.giveStream(stream, data);
    }

    public void feedStreamToAllPlayers(ServerStream stream, ServerDatalet voiceData) {
        EntityPlayerMP speaker = voiceData.player;
        List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        if (voiceData.end) {
            for (int i = 0; i < players.size(); ++i) {
                EntityPlayerMP target = (EntityPlayerMP)players.get(i);
                if (target.getEntityId() == speaker.getEntityId()) continue;
                this.voiceChat.getVoiceServer().sendVoiceEnd(target, voiceData.id);
            }
        } else {
            for (int i = 0; i < players.size(); ++i) {
                EntityPlayerMP target = (EntityPlayerMP)players.get(i);
                if (target.getEntityId() == speaker.getEntityId()) continue;
                this.entityHandler.whileSpeaking(stream, speaker, target);
                this.voiceChat.getVoiceServer().sendChunkVoiceData(target, voiceData.id, false, voiceData.data, voiceData.divider);
            }
        }
    }

    public void feedStreamToPlayer(ServerStream stream, ServerDatalet voiceData, EntityPlayerMP target, boolean direct) {
        EntityPlayerMP speaker = voiceData.player;
        if (voiceData.end) {
            if (this.voiceChat.getVoiceServer() != null && target != null) {
                this.voiceChat.getVoiceServer().sendVoiceEnd(target, stream.id);
            } else {
                this.entityHandler.whileSpeaking(stream, speaker, target);
                this.voiceChat.getVoiceServer().sendChunkVoiceData(target, voiceData.id, direct, voiceData.data, voiceData.divider);
            }
        }
    }

    public void feedStreamToWorld(ServerStream stream, ServerDatalet voiceData) {
        EntityPlayerMP speaker = voiceData.player;
        List players = speaker.worldObj.playerEntities;
        if (voiceData.end) {
            for (int i = 0; i < players.size(); ++i) {
                EntityPlayerMP target = (EntityPlayerMP)players.get(i);
                if (target.getEntityId() == speaker.getEntityId() || this.voiceChat.getVoiceServer() == null || target == null) continue;
                this.voiceChat.getVoiceServer().sendVoiceEnd(target, stream.id);
            }
        } else {
            for (int i = 0; i < players.size(); ++i) {
                EntityPlayerMP target = (EntityPlayerMP)players.get(i);
                if (target.getEntityId() == speaker.getEntityId()) continue;
                this.entityHandler.whileSpeaking(stream, speaker, target);
                this.voiceChat.getVoiceServer().sendChunkVoiceData(target, voiceData.id, false, voiceData.data, voiceData.divider);
            }
        }
    }

    public void feedWithinEntityWithRadius(ServerStream stream, ServerDatalet voiceData, int distance) {
        EntityPlayerMP speaker = stream.player;
        List players = speaker.worldObj.playerEntities;
        if (voiceData.end) {
            for (int i = 0; i < players.size(); ++i) {
                double d5;
                double d6;
                double d4;
                EntityPlayerMP target = (EntityPlayerMP)players.get(i);
                if (target.getEntityId() == speaker.getEntityId() || (d4 = speaker.posX - target.posX) * d4 + (d5 = speaker.posY - target.posY) * d5 + (d6 = speaker.posZ - target.posZ) * d6 >= (double)(distance * distance) || this.voiceChat.getVoiceServer() == null || target == null) continue;
                this.voiceChat.getVoiceServer().sendVoiceEnd(target, stream.id);
            }
        } else {
            for (int i = 0; i < players.size(); ++i) {
                double d5;
                double distanceBetween;
                double d6;
                double d4;
                EntityPlayerMP target = (EntityPlayerMP)players.get(i);
                if (target.getEntityId() == speaker.getEntityId() || (distanceBetween = (d4 = speaker.posX - target.posX) * d4 + (d5 = speaker.posY - target.posY) * d5 + (d6 = speaker.posZ - target.posZ) * d6) >= (double)(distance * distance)) continue;
                this.entityHandler.whileSpeaking(stream, speaker, target);
                this.voiceChat.getVoiceServer().sendChunkVoiceData(target, voiceData.id, true, voiceData.data, voiceData.divider);
                if (stream.tick % this.voiceChat.serverSettings.positionUpdateRate == 0) {
                    if (distanceBetween > 4096.0) {
                        this.voiceChat.getVoiceServer().sendEntityPosition(target, speaker.getEntityId(), speaker.posX, speaker.posY, speaker.posZ);
                    }
                    stream.tick = 0;
                }
                ++stream.tick;
            }
        }
    }

    private final String generateSource(ServerDatalet let) {
        return Integer.toString(let.id);
    }

    public ServerStream getStream(int entityId) {
        return this.streaming.get(entityId);
    }

    public void giveEntity(EntityPlayerMP receiver, EntityPlayerMP speaker) {
        this.voiceChat.getServerNetwork().sendEntityData(receiver, speaker.getEntityId(), speaker.getName(), speaker.posX, speaker.posY, speaker.posZ);
    }

    public void giveStream(ServerStream stream, ServerDatalet let) {
        VoiceChatAPI.instance().bus().post((Event)new ServerStreamEvent.StreamFeed(this, stream, let));
        stream.lastUpdated = System.currentTimeMillis();
        if (let.end) {
            this.killStream(stream);
        }
    }

    public void init() {
        this.running = true;
        this.entityHandler = new EntityHandler(this.voiceChat);
        this.mutedPlayers = new ArrayList<UUID>();
        this.dataQueue = new ConcurrentLinkedQueue();
        this.currentStreams = new ArrayList<ServerStream>();
        this.streaming = new ConcurrentHashMap();
        this.chatModeMap = new HashMap();
        this.receivedEntityData = new HashMap();
        this.treadQueue = new Thread((Runnable)new ThreadDataQueue(this), "Stream Queue");
        this.treadQueue.start();
        this.threadUpdate = new Thread((Runnable)new ThreadDataUpdateStream(this), "Stream Update");
        this.threadUpdate.start();
    }

    public void killStream(ServerStream stream) {
        this.currentStreams.remove(stream);
        this.streaming.remove(stream.id);
        VoiceChatAPI.instance().bus().post((Event)new ServerStreamEvent.StreamDestroyed(this, stream));
    }

    public ServerStream newDatalet(ServerDatalet let) {
        return this.streaming.get(let.id);
    }

    public void reset() {
        this.running = false;
        this.currentStreams.clear();
        this.chatModeMap.clear();
        this.dataQueue.clear();
        this.mutedPlayers.clear();
        this.receivedEntityData.clear();
        this.streaming.clear();
    }
}


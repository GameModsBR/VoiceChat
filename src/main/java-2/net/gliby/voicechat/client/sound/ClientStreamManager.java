/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.audio.SoundCategory
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.GuiScreenOptionsSounds
 *  net.minecraft.client.multiplayer.WorldClient
 *  net.minecraft.client.settings.GameSettings
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  org.lwjgl.util.vector.Vector3f
 */
package net.gliby.voicechat.client.sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.sound.Datalet;
import net.gliby.voicechat.client.sound.JitterBuffer;
import net.gliby.voicechat.client.sound.Recorder;
import net.gliby.voicechat.client.sound.SoundPreProcessor;
import net.gliby.voicechat.client.sound.SoundSystemWrapper;
import net.gliby.voicechat.client.sound.thread.ThreadSoundQueue;
import net.gliby.voicechat.client.sound.thread.ThreadUpdateStream;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.util.vector.Vector3f;

public class ClientStreamManager {
    public static AudioFormat universalAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0f, 16, 1, 2, 16000.0f, false);
    public static Map<Integer, String> playerMutedData = new HashMap<Integer, String>();
    public List<ClientStream> currentStreams = new ArrayList<ClientStream>();
    public List<Integer> playersMuted = new ArrayList<Integer>();
    public ConcurrentLinkedQueue<Datalet> queue = new ConcurrentLinkedQueue();
    public ConcurrentHashMap<Integer, ClientStream> streaming = new ConcurrentHashMap();
    public final SoundPreProcessor soundPreProcessor;
    public ConcurrentHashMap<Integer, PlayerProxy> playerData = new ConcurrentHashMap();
    private Thread threadUpdate;
    private ThreadSoundQueue threadQueue;
    private final Minecraft mc;
    private final VoiceChatClient voiceChat;
    private boolean volumeControlActive;
    private final float volumeValue = 0.15f;
    private float WEATHER;
    private float RECORDS;
    private float BLOCKS;
    private float MOBS;
    private float ANIMALS;

    public static AudioFormat getUniversalAudioFormat() {
        return universalAudioFormat;
    }

    public ClientStreamManager(Minecraft mc, VoiceChatClient voiceChatClient) {
        this.mc = mc;
        this.voiceChat = voiceChatClient;
        this.soundPreProcessor = new SoundPreProcessor(voiceChatClient, mc);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addQueue(byte[] decoded_data, boolean global, int id) {
        if (!this.playersMuted.contains(id)) {
            this.queue.offer(new Datalet(global, id, decoded_data));
            ThreadSoundQueue threadSoundQueue = this.threadQueue;
            synchronized (threadSoundQueue) {
                this.threadQueue.notify();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addStreamSafe(ClientStream stream) {
        this.streaming.put(stream.id, stream);
        Thread thread = this.threadUpdate;
        synchronized (thread) {
            this.threadUpdate.notify();
        }
        String entityName = stream.player.entityName();
        for (int i = 0; i < this.voiceChat.getTestPlayers().length; ++i) {
            String name = this.voiceChat.getTestPlayers()[i];
            if (!stream.player.equals(name)) continue;
            stream.special = 2;
        }
        if (this.voiceChat.specialPlayers.containsKey(entityName)) {
            stream.special = this.voiceChat.specialPlayers.get(entityName);
        }
        if (!this.containsStream(stream.id)) {
            ArrayList<ClientStream> streams = new ArrayList<ClientStream>(this.currentStreams);
            streams.add(stream);
            Collections.sort(streams, new ClientStream.PlayableStreamComparator());
            this.currentStreams.removeAll(this.currentStreams);
            this.currentStreams.addAll(streams);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void alertEnd(int id) {
        if (!this.playersMuted.contains(id)) {
            this.queue.offer(new Datalet(false, id, null));
            ThreadSoundQueue threadSoundQueue = this.threadQueue;
            synchronized (threadSoundQueue) {
                this.threadQueue.notify();
            }
        }
    }

    public boolean containsStream(int id) {
        ClientStream currentStream = this.streaming.get(id);
        for (int i = 0; i < this.currentStreams.size(); ++i) {
            ClientStream stream = this.currentStreams.get(i);
            String currentName = currentStream.player.entityName();
            String otherName = stream.player.entityName();
            if (stream.player.entityName() != null && currentStream.player.entityName() != null && currentName.equals(otherName)) {
                return true;
            }
            if (stream.id != id) continue;
            return true;
        }
        return false;
    }

    public void createStream(Datalet data) {
        String identifier = this.generateSource(data.id);
        PlayerProxy player = this.getPlayerData(data.id);
        if (data.direct) {
            Vector3f position = player.position();
            this.voiceChat.sndSystem.rawDataStream(universalAudioFormat, true, identifier, position.x, position.y, position.z, 2, this.voiceChat.getSettings().getSoundDistance());
        } else {
            this.voiceChat.sndSystem.rawDataStream(universalAudioFormat, true, identifier, (float)this.mc.thePlayer.posX, (float)this.mc.thePlayer.posY, (float)this.mc.thePlayer.posZ, 2, this.voiceChat.getSettings().getSoundDistance());
        }
        this.voiceChat.sndSystem.setPitch(identifier, 1.0f);
        this.voiceChat.sndSystem.setVolume(identifier, this.voiceChat.getSettings().getWorldVolume());
        this.addStreamSafe(new ClientStream(player, data.id, data.direct));
        this.giveStream(data);
    }

    private String generateSource(int let) {
        return "" + let;
    }

    private PlayerProxy getPlayerData(int entityId) {
        PlayerProxy proxy = this.playerData.get(entityId);
        EntityPlayer entity = (EntityPlayer)this.mc.theWorld.getEntityByID(entityId);
        if (proxy == null) {
            if (entity != null) {
                proxy = new PlayerProxy(entity, entity.getEntityId(), entity.getName(), entity.posX, entity.posY, entity.posZ);
            } else {
                VoiceChat.getLogger().error("Major error, no entity found for player.");
                proxy = new PlayerProxy(null, entityId, "" + entityId, 0.0, 0.0, 0.0);
            }
            this.playerData.put(entityId, proxy);
        } else if (entity != null) {
            proxy.setPlayer(entity);
            proxy.setName(entity.getName());
        }
        return proxy;
    }

    public SoundPreProcessor getSoundPreProcessor() {
        return this.soundPreProcessor;
    }

    public void giveEnd(int id) {
        ClientStream stream = this.streaming.get(id);
        if (stream != null) {
            stream.needsEnd = true;
        }
    }

    public void giveStream(Datalet data) {
        ClientStream stream = this.streaming.get(data.id);
        if (stream != null) {
            String identifier = this.generateSource(data.id);
            stream.update(data, (int)(System.currentTimeMillis() - stream.lastUpdated));
            stream.buffer.push(data.data);
            stream.buffer.updateJitter(stream.getJitterRate());
            if (stream.buffer.isReady() || stream.needsEnd) {
                this.voiceChat.sndSystem.flush(identifier);
                this.voiceChat.sndSystem.feedRawAudioData(identifier, stream.buffer.get());
                stream.buffer.clearBuffer(stream.getJitterRate());
            }
            stream.lastUpdated = System.currentTimeMillis();
        }
    }

    public void init() {
        this.threadQueue = new ThreadSoundQueue(this);
        Thread thread = new Thread((Runnable)this.threadQueue, "Client Stream Queue");
        thread.start();
        this.threadUpdate = new Thread((Runnable)new ThreadUpdateStream(this, this.voiceChat), "Client Stream Updater");
        this.threadUpdate.start();
    }

    public void killStream(ClientStream stream) {
        if (stream != null) {
            ArrayList<ClientStream> streams = new ArrayList<ClientStream>(this.currentStreams);
            streams.remove(stream);
            Collections.sort(streams, new ClientStream.PlayableStreamComparator());
            this.currentStreams.removeAll(this.currentStreams);
            this.currentStreams.addAll(streams);
            this.currentStreams.remove(stream);
            Collections.sort(this.currentStreams, new ClientStream.PlayableStreamComparator());
            this.streaming.remove(stream.id);
        }
    }

    public boolean newDatalet(Datalet let) {
        return !this.streaming.containsKey(let.id);
    }

    public void reload() {
        if (!this.currentStreams.isEmpty()) {
            VoiceChatClient.getLogger().info("Reloading SoundManager, removing all active streams.");
            for (int i = 0; i < this.currentStreams.size(); ++i) {
                ClientStream stream = this.currentStreams.get(i);
                this.killStream(stream);
            }
        }
    }

    public void reset() {
        this.voiceChat.setRecorderActive(false);
        this.voiceChat.recorder.stop();
        this.volumeControlStop();
        this.queue.clear();
        this.streaming.clear();
        this.currentStreams.clear();
        this.playerData.clear();
    }

    public void volumeControlStart() {
        if (!(this.mc.currentScreen instanceof GuiScreenOptionsSounds) && !this.volumeControlActive) {
            this.WEATHER = this.mc.gameSettings.getSoundLevel(SoundCategory.WEATHER);
            this.RECORDS = this.mc.gameSettings.getSoundLevel(SoundCategory.RECORDS);
            this.BLOCKS = this.mc.gameSettings.getSoundLevel(SoundCategory.BLOCKS);
            this.MOBS = this.mc.gameSettings.getSoundLevel(SoundCategory.MOBS);
            this.ANIMALS = this.mc.gameSettings.getSoundLevel(SoundCategory.PLAYERS);
            if (this.mc.gameSettings.getSoundLevel(SoundCategory.WEATHER) > 0.15f) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.WEATHER, 0.15f);
            }
            if (this.mc.gameSettings.getSoundLevel(SoundCategory.RECORDS) > 0.15f) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.RECORDS, 0.15f);
            }
            if (this.mc.gameSettings.getSoundLevel(SoundCategory.BLOCKS) > 0.15f) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.BLOCKS, 0.15f);
            }
            if (this.mc.gameSettings.getSoundLevel(SoundCategory.MOBS) > 0.15f) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.MOBS, 0.15f);
            }
            if (this.mc.gameSettings.getSoundLevel(SoundCategory.ANIMALS) > 0.15f) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.ANIMALS, 0.15f);
            }
            this.volumeControlActive = true;
        }
    }

    public void volumeControlStop() {
        if (this.volumeControlActive) {
            this.mc.gameSettings.setSoundLevel(SoundCategory.WEATHER, this.WEATHER);
            this.mc.gameSettings.setSoundLevel(SoundCategory.RECORDS, this.RECORDS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.BLOCKS, this.BLOCKS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.MOBS, this.MOBS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.ANIMALS, this.ANIMALS);
            this.volumeControlActive = false;
        }
    }
}


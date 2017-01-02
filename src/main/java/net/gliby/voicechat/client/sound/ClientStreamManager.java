package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.thread.ThreadSoundQueue;
import net.gliby.voicechat.client.sound.thread.ThreadUpdateStream;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.SoundSystem;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientStreamManager {

    public static AudioFormat universalAudioFormat = new AudioFormat(Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F, false);
    public static Map<Integer, String> playerMutedData = new HashMap<Integer, String>();
    public final SoundPreProcessor soundPreProcessor;
    private final Minecraft mc;
    private final VoiceChatClient voiceChat;
    private final float volumeValue = 0.15F;
    public List<ClientStream> currentStreams = new ArrayList<ClientStream>();
    public List<Integer> playersMuted = new ArrayList<Integer>();
    public ConcurrentLinkedQueue<Datalet> queue = new ConcurrentLinkedQueue<Datalet>();
    public ConcurrentHashMap<Integer, ClientStream> streaming = new ConcurrentHashMap<Integer, ClientStream>();
    public ConcurrentHashMap<Integer, PlayerProxy> playerData = new ConcurrentHashMap<Integer, PlayerProxy>();
    private Thread threadUpdate;
    private ThreadSoundQueue threadQueue;
    private boolean volumeControlActive;
    private float WEATHER;
    private float RECORDS;
    private float BLOCKS;
    private float MOBS;
    private float ANIMALS;


    public ClientStreamManager(Minecraft mc, VoiceChatClient voiceChatClient) {
        this.mc = mc;
        this.voiceChat = voiceChatClient;
        this.soundPreProcessor = new SoundPreProcessor(voiceChatClient, mc);
    }

    public static AudioFormat getUniversalAudioFormat() {
        return universalAudioFormat;
    }

    public void addQueue(byte[] decoded_data, boolean global, int id) {
        if (!this.playersMuted.contains(id)) {
            this.queue.offer(new Datalet(global, id, decoded_data));
            ThreadSoundQueue var4 = this.threadQueue;
            synchronized (this.threadQueue) {
                this.threadQueue.notify();
            }
        }

    }

    private void addStreamSafe(ClientStream stream) {
        this.streaming.put(stream.id, stream);
        Thread entityName = this.threadUpdate;
        synchronized (this.threadUpdate) {
            this.threadUpdate.notify();
        }

        String var6 = stream.player.entityName();

        for (int streams = 0; streams < this.voiceChat.getTestPlayers().length; ++streams) {
            String name = this.voiceChat.getTestPlayers()[streams];
            if (stream.player.equals(name)) {
                stream.special = 2;
            }
        }

        if (this.voiceChat.specialPlayers.containsKey(var6)) {
            stream.special = this.voiceChat.specialPlayers.get(var6);
        }

        if (!this.containsStream(stream.id)) {
            ArrayList<ClientStream> var7 = new ArrayList<ClientStream>(this.currentStreams);
            var7.add(stream);
            Collections.sort(var7, new ClientStream.PlayableStreamComparator());
            // TODO Is this intended?
            this.currentStreams.removeAll(this.currentStreams);
            this.currentStreams.addAll(var7);
        }

    }

    public void alertEnd(int id) {
        if (!this.playersMuted.contains(id)) {
            this.queue.offer(new Datalet(false, id, null));
            ThreadSoundQueue var2 = this.threadQueue;
            synchronized (this.threadQueue) {
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

            if (stream.id == id) {
                return true;
            }
        }

        return false;
    }

    public void createStream(Datalet data) {
        String identifier = this.generateSource(data.id);
        PlayerProxy player = this.getPlayerData(data.id);
        if (data.direct) {
            Vector3f position = player.position();
            this.voiceChat.sndSystem.rawDataStream(universalAudioFormat, true, identifier, position.x, position.y, position.z, 2, (float) this.voiceChat.getSettings().getSoundDistance());
        } else {
            this.voiceChat.sndSystem.rawDataStream(universalAudioFormat, true, identifier, (float) this.mc.thePlayer.posX, (float) this.mc.thePlayer.posY, (float) this.mc.thePlayer.posZ, 2, (float) this.voiceChat.getSettings().getSoundDistance());
        }

        this.voiceChat.sndSystem.setPitch(identifier, 1.0F);
        this.voiceChat.sndSystem.setVolume(identifier, this.voiceChat.getSettings().getWorldVolume());
        this.addStreamSafe(new ClientStream(player, data.id, data.direct));
        this.giveStream(data);
    }

    private String generateSource(int let) {
        return "" + let;
    }

    private PlayerProxy getPlayerData(int entityId) {
        PlayerProxy proxy = this.playerData.get(entityId);
        EntityPlayer entity = (EntityPlayer) this.mc.theWorld.getEntityByID(entityId);
        if (proxy == null) {
            if (entity != null) {
                proxy = new PlayerProxy(entity, entity.getEntityId(), entity.getName(), entity.posX, entity.posY, entity.posZ);
            } else {
                VoiceChat.getLogger().error("Major error, no entity found for player.");
                proxy = new PlayerProxy(null, entityId, "" + entityId, 0.0D, 0.0D, 0.0D);
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
            stream.update(data, (int) (System.currentTimeMillis() - stream.lastUpdated));
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
        Thread thread = new Thread(this.threadQueue = new ThreadSoundQueue(this), "Client Stream Queue");
        thread.start();
        this.threadUpdate = new Thread(new ThreadUpdateStream(this, this.voiceChat), "Client Stream Updater");
        this.threadUpdate.start();
    }

    public void killStream(ClientStream stream) {
        if (stream != null) {
            ArrayList<ClientStream> streams = new ArrayList<ClientStream>(this.currentStreams);
            streams.remove(stream);
            Collections.sort(streams, new ClientStream.PlayableStreamComparator());
            //TODO Is this intended? Why not clear()?
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
            this.MOBS = this.mc.gameSettings.getSoundLevel(SoundCategory.HOSTILE);
            this.ANIMALS = this.mc.gameSettings.getSoundLevel(SoundCategory.PLAYERS);
            if (this.mc.gameSettings.getSoundLevel(SoundCategory.WEATHER) > 0.15F) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.WEATHER, 0.15F);
            }

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.RECORDS) > 0.15F) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.RECORDS, 0.15F);
            }

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.BLOCKS) > 0.15F) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.BLOCKS, 0.15F);
            }

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.HOSTILE) > 0.15F) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.HOSTILE, 0.15F);
            }

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.NEUTRAL) > 0.15F) {
                this.mc.gameSettings.setSoundLevel(SoundCategory.NEUTRAL, 0.15F);
            }

            this.volumeControlActive = true;
        }

    }

    public void volumeControlStop() {
        if (this.volumeControlActive) {
            this.mc.gameSettings.setSoundLevel(SoundCategory.WEATHER, this.WEATHER);
            this.mc.gameSettings.setSoundLevel(SoundCategory.RECORDS, this.RECORDS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.BLOCKS, this.BLOCKS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.HOSTILE, this.MOBS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.NEUTRAL, this.ANIMALS);
            this.volumeControlActive = false;
        }

    }

}

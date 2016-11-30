package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.common.PlayerProxy;

import java.util.Comparator;

public class ClientStream {

    public final int id;
    public int special;
    public boolean needsEnd;
    public boolean direct;
    public long lastUpdated;
    public JitterBuffer buffer;
    public PlayerProxy player;
    public boolean dirty;


    public ClientStream(PlayerProxy proxy, int id, boolean direct) {
        this.id = id;
        this.direct = direct;
        this.lastUpdated = System.currentTimeMillis();
        this.player = proxy;
        this.buffer = new JitterBuffer(ClientStreamManager.universalAudioFormat, 0);
    }

    public ClientStream(PlayerProxy proxy, int id, boolean direct, int special) {
        this.id = id;
        this.direct = direct;
        this.lastUpdated = System.currentTimeMillis();
        this.buffer = new JitterBuffer(ClientStreamManager.universalAudioFormat, 0);
        this.special = special;
    }

    public String generateSource() {
        return "" + this.id;
    }

    public int getJitterRate() {
        return this.getLastTimeUpdatedMS();
    }

    public int getLastTimeUpdatedMS() {
        return (int) (System.currentTimeMillis() - this.lastUpdated);
    }

    public void update(Datalet data, int l) {
        if (this.direct != data.direct) {
            this.dirty = true;
        }

        this.direct = data.direct;
    }

    public static class PlayableStreamComparator implements Comparator<ClientStream> {

        @Override
        public int compare(ClientStream a, ClientStream b) {
            return a.id > b.id ? 1 : (a.id < b.id ? -1 : 0);
        }
    }
}

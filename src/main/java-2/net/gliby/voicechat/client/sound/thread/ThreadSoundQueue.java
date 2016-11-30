/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.client.sound.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.Datalet;

public class ThreadSoundQueue
implements Runnable {
    private final ClientStreamManager sndManager;
    private final Object notifier = new Object();

    public ThreadSoundQueue(ClientStreamManager sndManager) {
        this.sndManager = sndManager;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        do {
            Object data;
            if (!this.sndManager.queue.isEmpty()) {
                boolean end;
                data = this.sndManager.queue.poll();
                if (data == null) continue;
                boolean bl = end = data.data == null;
                if (this.sndManager.newDatalet((Datalet)data) && !end) {
                    this.sndManager.createStream((Datalet)data);
                    continue;
                }
                if (end) {
                    this.sndManager.giveEnd(data.id);
                    continue;
                }
                this.sndManager.giveStream((Datalet)data);
                continue;
            }
            try {
                data = this;
                synchronized (data) {
                    this.wait();
                    continue;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            break;
        } while (true);
    }
}


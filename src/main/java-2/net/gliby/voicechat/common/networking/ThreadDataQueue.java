/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.common.networking;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;

public class ThreadDataQueue
implements Runnable {
    private final ServerStreamManager manager;

    public ThreadDataQueue(ServerStreamManager manager) {
        this.manager = manager;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        while (this.manager.running) {
            Object voiceData;
            if (!this.manager.dataQueue.isEmpty()) {
                voiceData = this.manager.dataQueue.poll();
                ServerStream stream = this.manager.newDatalet((ServerDatalet)voiceData);
                if (stream == null) {
                    this.manager.createStream((ServerDatalet)voiceData);
                    continue;
                }
                this.manager.giveStream(stream, (ServerDatalet)voiceData);
                continue;
            }
            voiceData = this;
            synchronized (voiceData) {
                try {
                    this.wait(1);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }
}


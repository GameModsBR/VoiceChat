/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 */
package net.gliby.voicechat.common.networking;

import java.util.List;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.entity.player.EntityPlayerMP;

public class ThreadDataUpdateStream
implements Runnable {
    private static final int ARBITRARY_TIMEOUT = 350;
    private final ServerStreamManager dataManager;

    public ThreadDataUpdateStream(ServerStreamManager dataManager) {
        this.dataManager = dataManager;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        while (this.dataManager.running) {
            if (!this.dataManager.currentStreams.isEmpty()) {
                for (int i = 0; i < this.dataManager.currentStreams.size(); ++i) {
                    ServerStream stream = this.dataManager.currentStreams.get(i);
                    int duration = stream.getLastTimeUpdated();
                    if (duration <= 350 || duration <= stream.player.ping * 2) continue;
                    this.dataManager.killStream(stream);
                }
            }
            try {
                ThreadDataUpdateStream i = this;
                synchronized (i) {
                    this.wait(12);
                    continue;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}


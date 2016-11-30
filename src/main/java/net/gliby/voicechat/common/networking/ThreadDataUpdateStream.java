package net.gliby.voicechat.common.networking;

public class ThreadDataUpdateStream implements Runnable {

    private static final int ARBITRARY_TIMEOUT = 350;
    private final ServerStreamManager dataManager;


    public ThreadDataUpdateStream(ServerStreamManager dataManager) {
        this.dataManager = dataManager;
    }

    public void run() {
        while (this.dataManager.running) {
            if (!this.dataManager.currentStreams.isEmpty()) {
                for (int e = 0; e < this.dataManager.currentStreams.size(); ++e) {
                    ServerStream stream = this.dataManager.currentStreams.get(e);
                    int duration = stream.getLastTimeUpdated();
                    if (duration > 350 && duration > stream.player.ping * 2) {
                        this.dataManager.killStream(stream);
                    }
                }
            }

            try {
                synchronized (this) {
                    this.wait(12L);
                }
            } catch (InterruptedException var6) {
                var6.printStackTrace();
            }
        }

    }
}

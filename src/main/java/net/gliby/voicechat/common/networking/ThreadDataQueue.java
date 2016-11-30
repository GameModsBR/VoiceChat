package net.gliby.voicechat.common.networking;

import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;

public class ThreadDataQueue implements Runnable {

   private final ServerStreamManager manager;


   public ThreadDataQueue(ServerStreamManager manager) {
      this.manager = manager;
   }

   public void run() {
      while(this.manager.running) {
         if(!this.manager.dataQueue.isEmpty()) {
            ServerDatalet voiceData = this.manager.dataQueue.poll();
            ServerStream e;
            if((e = this.manager.newDatalet(voiceData)) == null) {
               this.manager.createStream(voiceData);
            } else {
               this.manager.giveStream(e, voiceData);
            }
         } else {
            synchronized(this) {
               try {
                  this.wait(1L);
               } catch (InterruptedException var4) {
                  var4.printStackTrace();
               }
            }
         }
      }

   }
}

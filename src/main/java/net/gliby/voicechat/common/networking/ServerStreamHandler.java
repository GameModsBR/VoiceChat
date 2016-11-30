package net.gliby.voicechat.common.networking;

import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerStreamHandler {

   VoiceChatServer voiceChat;


   public ServerStreamHandler(VoiceChatServer voiceChat) {
      this.voiceChat = voiceChat;
   }

   @SubscribeEvent
   public void createdStream(ServerStreamEvent.StreamCreated event) {
      int chatMode = this.voiceChat.getServerSettings().getDefaultChatMode();
      if(event.streamManager.chatModeMap.containsKey(event.voiceLet.player.getPersistentID())) {
         chatMode = ((Integer)event.streamManager.chatModeMap.get(event.voiceLet.player.getPersistentID())).intValue();
      }

      event.stream.chatMode = chatMode;
   }

   @SubscribeEvent
   public void feedStream(ServerStreamEvent.StreamFeed event) {
      if(event.stream.dirty && event.streamManager.chatModeMap.containsKey(event.stream.player.getPersistentID())) {
         event.stream.chatMode = ((Integer)event.streamManager.chatModeMap.get(event.stream.player.getPersistentID())).intValue();
      }

      switch(event.stream.chatMode) {
      case 0:
         event.streamManager.feedWithinEntityWithRadius(event.stream, event.voiceLet, this.voiceChat.getServerSettings().getSoundDistance());
         break;
      case 1:
         event.streamManager.feedStreamToWorld(event.stream, event.voiceLet);
         break;
      case 2:
         event.streamManager.feedStreamToAllPlayers(event.stream, event.voiceLet);
      }

   }

   @SubscribeEvent
   public void killStream(ServerStreamEvent.StreamDestroyed event) {}
}

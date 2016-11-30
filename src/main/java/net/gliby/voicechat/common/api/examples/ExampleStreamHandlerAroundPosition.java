package net.gliby.voicechat.common.api.examples;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ExampleStreamHandlerAroundPosition {

    @SubscribeEvent
    public void feedStream(ServerStreamEvent.StreamFeed event) {
        event.stream.player.mcServer.getConfigurationManager();
        this.feedStreamPositionWithRadius(event.streamManager, event.stream, event.voiceLet, event.stream.player.worldObj, 0.0D, 128.0D, 0.0D, VoiceChat.getServerInstance().getServerSettings().getSoundDistance());
    }

    public void feedStreamPositionWithRadius(ServerStreamManager streamManager, ServerStream stream, ServerDatalet voiceData, World world, double x, double y, double z, int distance) {
        EntityPlayerMP speaker = stream.player;
        List players = world.playerEntities;

        for (int i = 0; i < players.size(); ++i) {
            EntityPlayerMP target = (EntityPlayerMP) players.get(i);
            if (target.getEntityId() != speaker.getEntityId()) {
                double d4 = x - target.posX;
                double d5 = y - target.posY;
                double d6 = z - target.posZ;
                if (d4 * d4 + d5 * d5 + d6 * d6 < (double) (distance * distance)) {
                    streamManager.feedStreamToPlayer(stream, voiceData, target, distance == VoiceChat.getServerInstance().getServerSettings().getSoundDistance());
                }
            }
        }

    }
}

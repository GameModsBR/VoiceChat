/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.management.ServerConfigurationManager
 *  net.minecraft.world.World
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package net.gliby.voicechat.common.api.examples;

import java.util.List;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.ServerSettings;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExampleStreamHandlerAroundPosition {
    @SubscribeEvent
    public void feedStream(ServerStreamEvent.StreamFeed event) {
        event.stream.player.mcServer.getConfigurationManager();
        this.feedStreamPositionWithRadius(event.streamManager, event.stream, event.voiceLet, event.stream.player.worldObj, 0.0, 128.0, 0.0, VoiceChat.getServerInstance().getServerSettings().getSoundDistance());
    }

    public void feedStreamPositionWithRadius(ServerStreamManager streamManager, ServerStream stream, ServerDatalet voiceData, World world, double x, double y, double z, int distance) {
        EntityPlayerMP speaker = stream.player;
        List players = world.playerEntities;
        for (int i = 0; i < players.size(); ++i) {
            double d6;
            double d4;
            double d5;
            EntityPlayerMP target = (EntityPlayerMP)players.get(i);
            if (target.getEntityId() == speaker.getEntityId() || (d4 = x - target.posX) * d4 + (d5 = y - target.posY) * d5 + (d6 = z - target.posZ) * d6 >= (double)(distance * distance)) continue;
            streamManager.feedStreamToPlayer(stream, voiceData, target, distance == VoiceChat.getServerInstance().getServerSettings().getSoundDistance());
        }
    }
}


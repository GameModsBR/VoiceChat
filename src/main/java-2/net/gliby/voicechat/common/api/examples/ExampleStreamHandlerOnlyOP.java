/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.management.ServerConfigurationManager
 *  net.minecraft.server.management.UserListEntry
 *  net.minecraft.server.management.UserListOps
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.IChatComponent
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package net.gliby.voicechat.common.api.examples;

import com.mojang.authlib.GameProfile;
import java.util.List;
import net.gliby.voicechat.common.api.VoiceChatAPI;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.server.management.UserListOps;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExampleStreamHandlerOnlyOP {
    public ExampleStreamHandlerOnlyOP() {
        VoiceChatAPI.instance().setCustomStreamHandler(this);
    }

    @SubscribeEvent
    public void createStream(ServerStreamEvent.StreamCreated event) {
        if (!this.isOP(event.stream.player)) {
            event.stream.player.addChatMessage((IChatComponent)new ChatComponentText("Only OP's are allowed to talk!"));
        }
    }

    @SubscribeEvent
    public void feedStream(ServerStreamEvent.StreamFeed event) {
        List players = event.stream.player.mcServer.getConfigurationManager().playerEntityList;
        EntityPlayerMP speaker = event.stream.player;
        if (this.isOP(speaker)) {
            for (int i = 0; i < players.size(); ++i) {
                EntityPlayerMP player = (EntityPlayerMP)players.get(i);
                if (!this.isOP(player) || player.getEntityId() == speaker.getEntityId()) continue;
                event.streamManager.feedStreamToPlayer(event.stream, event.voiceLet, player, false);
            }
        }
    }

    public boolean isOP(EntityPlayerMP player) {
        return player.mcServer.getConfigurationManager().getOppedPlayers().getEntry((Object)player.getGameProfile()) != null;
    }
}


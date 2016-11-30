/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 */
package net.gliby.voicechat.common.networking.voiceservers;

import java.util.HashMap;
import java.util.Map;
import net.gliby.voicechat.common.networking.voiceservers.VoiceServer;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class VoiceAuthenticatedServer
extends VoiceServer {
    public Map<String, EntityPlayerMP> waitingAuth = new HashMap<String, EntityPlayerMP>();

    public abstract void closeConnection(int var1);
}


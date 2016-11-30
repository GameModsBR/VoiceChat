/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.PlayerNotFoundException
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.IChatComponent
 */
package net.gliby.voicechat.common.commands;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class CommandChatMode
extends CommandBase {
    public List addTabCompletionOptions(ICommandSender sender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length == 1 ? CommandChatMode.getListOfStringsMatchingLastWord((String[])par2ArrayOfStr, (String[])new String[]{"distance", "global", "world"}) : (par2ArrayOfStr.length == 2 ? CommandChatMode.getListOfStringsMatchingLastWord((String[])par2ArrayOfStr, (String[])this.getListOfPlayerUsernames()) : null);
    }

    public String getChatMode(int chatMode) {
        return chatMode == 0 ? "distance" : (chatMode == 2 ? "global" : (chatMode == 1 ? "world" : "distance"));
    }

    protected int getChatModeFromCommand(ICommandSender par1ICommandSender, String par2Str) {
        return par2Str.equalsIgnoreCase("distance") || par2Str.startsWith("d") || par2Str.equalsIgnoreCase("0") ? 0 : (par2Str.equalsIgnoreCase("world") || par2Str.startsWith("w") || par2Str.equalsIgnoreCase("1") ? 1 : (par2Str.equalsIgnoreCase("global") || par2Str.startsWith("g") || par2Str.equalsIgnoreCase("2") ? 2 : 0));
    }

    public String getName() {
        return "vchatmode";
    }

    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/vchatmode <mode> or /vchatmode <mode> [player]";
    }

    protected String[] getListOfPlayerUsernames() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public int getRequiredPermissionLevel() {
        return 3;
    }

    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2) {
        return par2 == 1;
    }

    public void execute(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        if (par2ArrayOfStr.length > 0) {
            int chatMode = this.getChatModeFromCommand(par1ICommandSender, par2ArrayOfStr[0]);
            EntityPlayerMP player = null;
            try {
                player = par2ArrayOfStr.length >= 2 ? CommandChatMode.getPlayer((ICommandSender)par1ICommandSender, (String)par2ArrayOfStr[1]) : CommandChatMode.getCommandSenderAsPlayer((ICommandSender)par1ICommandSender);
            }
            catch (PlayerNotFoundException e) {
                e.printStackTrace();
            }
            if (player != null) {
                ServerStreamManager dataManager = VoiceChat.getServerInstance().getServerNetwork().getDataManager();
                dataManager.chatModeMap.put(player.getPersistentID(), chatMode);
                ServerStream stream = dataManager.getStream(player.getEntityId());
                if (stream != null) {
                    stream.dirty = true;
                }
                if (player != par1ICommandSender) {
                    CommandChatMode.notifyOperators((ICommandSender)par1ICommandSender, (ICommand)this, (String)(player.getName() + " set chat mode to " + this.getChatMode(chatMode).toUpperCase() + " (" + chatMode + ")"), (Object[])new Object[]{par2ArrayOfStr[0]});
                } else {
                    player.addChatMessage((IChatComponent)new ChatComponentText("Set own chat mode to " + this.getChatMode(chatMode).toUpperCase() + " (" + chatMode + ")"));
                    switch (chatMode) {
                        case 0: {
                            player.addChatMessage((IChatComponent)new ChatComponentText("Only players near you can hear you."));
                            break;
                        }
                        case 1: {
                            player.addChatMessage((IChatComponent)new ChatComponentText("Every player in this world can hear you"));
                            break;
                        }
                        case 2: {
                            player.addChatMessage((IChatComponent)new ChatComponentText("Every player can hear you."));
                        }
                    }
                }
            }
        }
    }
}


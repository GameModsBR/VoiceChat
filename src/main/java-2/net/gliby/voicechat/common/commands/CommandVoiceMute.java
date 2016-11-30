/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.CommandException
 *  net.minecraft.command.ICommand
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.command.WrongUsageException
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.IChatComponent
 */
package net.gliby.voicechat.common.commands;

import java.util.List;
import java.util.UUID;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class CommandVoiceMute
extends CommandBase {
    public List addTabCompletionOptions(ICommandSender sender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length == 1 ? CommandVoiceMute.getListOfStringsMatchingLastWord((String[])par2ArrayOfStr, (String[])this.getPlayers()) : null;
    }

    public String getName() {
        return "vmute";
    }

    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "Usage: /vmute <player>";
    }

    protected String[] getPlayers() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public int getRequiredPermissionLevel() {
        return 3;
    }

    public boolean isUsernameIndex(int par1) {
        return par1 == 0;
    }

    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && args[0].length() > 0) {
            ServerNetwork network = VoiceChat.getServerInstance().getServerNetwork();
            EntityPlayerMP player = CommandVoiceMute.getPlayer((ICommandSender)sender, (String)args[0]);
            if (player != null) {
                if (network.getDataManager().mutedPlayers.contains(player.getUniqueID())) {
                    network.getDataManager().mutedPlayers.remove(player.getUniqueID());
                    CommandVoiceMute.notifyOperators((ICommandSender)sender, (ICommand)this, (String)(player.getName() + " has been unmuted."), (Object[])new Object[]{args[0]});
                    player.addChatMessage((IChatComponent)new ChatComponentText("You have been unmuted!"));
                } else {
                    CommandVoiceMute.notifyOperators((ICommandSender)sender, (ICommand)this, (String)(player.getName() + " has been muted."), (Object[])new Object[]{args[0]});
                    network.getDataManager().mutedPlayers.add(player.getUniqueID());
                    player.addChatMessage((IChatComponent)new ChatComponentText("You have been voice muted, you cannot talk untill you have been unmuted."));
                }
            } else {
                sender.addChatMessage((IChatComponent)new ChatComponentText("Player not found for vmute."));
            }
        } else {
            throw new WrongUsageException(this.getCommandUsage(sender), new Object[0]);
        }
    }
}


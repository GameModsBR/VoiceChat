package net.gliby.voicechat.common.commands;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class CommandVoiceMute extends CommandBase {

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length == 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, this.getPlayers()) : null;
    }

    @Override
    public String getCommandName() {
        return "vmute";
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "Usage: /vmute <player>";
    }

    protected String[] getPlayers() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    public boolean isUsernameIndex(int par1) {
        return par1 == 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && args[0].length() > 0) {
            ServerNetwork network = VoiceChat.getServerInstance().getServerNetwork();
            EntityPlayerMP player = getPlayer(sender, args[0]);
            if (player != null) {
                if (network.getDataManager().mutedPlayers.contains(player.getUniqueID())) {
                    network.getDataManager().mutedPlayers.remove(player.getUniqueID());
                    notifyOperators(sender, this, player.getName() + " has been unmuted.", args[0]);
                    player.addChatMessage(new ChatComponentText("You have been unmuted!"));
                } else {
                    notifyOperators(sender, this, player.getName() + " has been muted.", args[0]);
                    network.getDataManager().mutedPlayers.add(player.getUniqueID());
                    player.addChatMessage(new ChatComponentText("You have been voice muted, you cannot talk untill you have been unmuted."));
                }
            } else {
                sender.addChatMessage(new ChatComponentText("Player not found for vmute."));
            }

        } else {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }
    }
}

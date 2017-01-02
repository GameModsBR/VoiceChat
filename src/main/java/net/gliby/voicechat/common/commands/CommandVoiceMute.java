package net.gliby.voicechat.common.commands;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class CommandVoiceMute extends CommandBase {

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length == 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, this.getPlayers(server)) : null;
    }

    @Override
    public String getCommandName() {
        return "vmute";
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "Usage: /vmute <player>";
    }

    protected String[] getPlayers(MinecraftServer server) {
        return server.getAllUsernames();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    public boolean isUsernameIndex(int par1) {
        return par1 == 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && args[0].length() > 0) {
            ServerNetwork network = VoiceChat.getServerInstance().getServerNetwork();
            EntityPlayerMP player = getPlayer(server, sender, args[0]);
            if (player != null) {
                if (network.getDataManager().mutedPlayers.contains(player.getUniqueID())) {
                    network.getDataManager().mutedPlayers.remove(player.getUniqueID());
                    notifyCommandListener(sender, this, player.getName() + " has been unmuted.", args[0]);
                    player.addChatMessage(new TextComponentString("You have been unmuted!"));
                } else {
                    notifyCommandListener(sender, this, player.getName() + " has been muted.", args[0]);
                    network.getDataManager().mutedPlayers.add(player.getUniqueID());
                    player.addChatMessage(new TextComponentString("You have been voice muted, you cannot talk untill you have been unmuted."));
                }
            } else {
                sender.addChatMessage(new TextComponentString("Player not found for vmute."));
            }

        } else {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }
    }
}

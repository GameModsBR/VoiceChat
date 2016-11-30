/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityOtherPlayerMP
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiOptionButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.GuiSlot
 *  net.minecraft.client.gui.GuiTextField
 *  net.minecraft.client.multiplayer.WorldClient
 *  net.minecraft.client.resources.I18n
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 *  org.lwjgl.input.Keyboard
 */
package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class GuiScreenLocalMute
        extends GuiScreen {
    protected GuiScreen parent;
    private List listPlayers;
    private GuiOptionButton doneButton;
    private GuiTextField playerTextField;
    private boolean playerNotFound;
    private ArrayList<String> autoCompletionNames;

    public GuiScreenLocalMute(GuiScreen par1GuiScreen, VoiceChatClient voiceChat) {
        this.parent = par1GuiScreen;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.actionPerformed(button.id);
    }

    private void actionPerformed(int button) {
        switch (button) {
            case 0: {
                this.playerNotFound = false;
                EntityPlayer entityPlayer = this.mc.theWorld.getPlayerEntityByName(this.playerTextField.getText().trim().replaceAll(" ", ""));
                if (entityPlayer != null) {
                    if (entityPlayer.isUser() || VoiceChatClient.getSoundManager().playersMuted.contains(entityPlayer.getEntityId()))
                        break;
                    VoiceChatClient.getSoundManager().playersMuted.add(entityPlayer.getEntityId());
                    VoiceChatClient.getSoundManager();
                    ClientStreamManager.playerMutedData.put(entityPlayer.getEntityId(), entityPlayer.getCommandSenderName());
                    break;
                }
                this.playerNotFound = true;
                break;
            }
            case 1: {
                this.mc.displayGuiScreen(this.parent);
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.listPlayers.drawScreen(par1, par2, par3);
        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.mutedPlayers", (Object[]) new Object[0]), this.width / 2, 16, -1);
        if (this.playerNotFound) {
            this.drawCenteredString(this.fontRendererObj, "\u00a7c" + I18n.format("commands.generic.player.notFound", (Object[]) new Object[0]), this.width / 2, this.height - 59, -1);
        }
        this.playerTextField.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.autoCompletionNames = new ArrayList<String>();
        int heightOffset = -9;
        this.playerTextField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, this.height - 57 - -9, 130, 20);
        this.playerTextField.setFocused(true);
        this.doneButton = new GuiOptionButton(0, this.width / 2 + 32, this.height - 57 - -9, 98, 20, I18n.format("menu.add", (Object[]) new Object[0]));
        @SuppressWarnings("unchecked")
        java.util.List<GuiButton> buttonList = this.buttonList;
        buttonList.add(this.doneButton);
        this.doneButton = new GuiOptionButton(1, this.width / 2 - 75, this.height - 32 - -9, I18n.format("gui.done", (Object[]) new Object[0]));
        buttonList.add(this.doneButton);
        this.listPlayers = new List();
        this.listPlayers.registerScrollButtons(7, 8);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.playerNotFound = false;
        this.playerTextField.textboxKeyTyped(par1, par2);
        try {
            super.keyTyped(par1, par2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (par2) {
            case 28: {
                this.actionPerformed(0);
                break;
            }
            case 15: {
                if (this.autoCompletionNames.size() > 0) {
                    this.shuffleCompleition();
                    break;
                }
                this.autoCompletionNames.clear();
                for (Object obj : this.mc.theWorld.playerEntities.toArray()) {
                    String s2;
                    if (!(obj instanceof EntityOtherPlayerMP) || !(s2 = ((EntityOtherPlayerMP) obj).getCommandSenderName()).toLowerCase().startsWith(this.playerTextField.getText().toLowerCase().trim().replaceAll(" ", "")))
                        continue;
                    this.autoCompletionNames.add(s2);
                }
                this.shuffleCompleition();
                break;
            }
            default: {
                this.autoCompletionNames.clear();
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    private void shuffleCompleition() {
        if (this.autoCompletionNames.iterator().hasNext()) {
            String name = this.autoCompletionNames.iterator().next();
            this.autoCompletionNames.add(name);
            this.playerTextField.setText(name);
            this.autoCompletionNames.remove(name);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.listPlayers.handleMouseInput();
    }

    @Override
    public void updateScreen() {
        this.playerTextField.updateCursorCounter();
    }

    @SideOnly(value = Side.CLIENT)
    class List
            extends GuiSlot {
        private final Rectangle buttonCross;

        public List() {
            super(GuiScreenLocalMute.this.mc, GuiScreenLocalMute.this.width, GuiScreenLocalMute.this.height, 32, GuiScreenLocalMute.this.height - 65 + 4, 18);
            this.buttonCross = new Rectangle(0, 0, 20, 20);
        }

        @Override
        protected void drawBackground() {
            GuiScreenLocalMute.this.drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int par2, int p_180791_4_, int p_180791_5_, int p_180791_6_) {
            VoiceChatClient.getSoundManager();
            GuiScreenLocalMute.this.drawCenteredString(GuiScreenLocalMute.this.fontRendererObj, ClientStreamManager.playerMutedData.get(VoiceChatClient.getSoundManager().playersMuted.get(entryID)), this.width / 2, par2 + 1, 16777215);
            GuiScreenLocalMute.this.drawCenteredString(GuiScreenLocalMute.this.fontRendererObj, "\u00a7lX", this.width / 2 + 88, par2 + 3, 16711680);
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            if (isDoubleClick) {
                VoiceChatClient.getSoundManager().playersMuted.remove(slotIndex);
                VoiceChatClient.getSoundManager();
                ClientStreamManager.playerMutedData.remove(slotIndex);
            }
        }

        @Override
        protected int getContentHeight() {
            return this.getSize() * 18;
        }

        @Override
        protected int getSize() {
            return VoiceChatClient.getSoundManager().playersMuted.size();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return true;
        }
    }

}


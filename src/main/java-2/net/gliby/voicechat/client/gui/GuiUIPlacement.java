/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.renderer.Tessellator
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.client.resources.I18n
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.Configuration;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.gui.EnumUIPlacement;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiPlaceableInterface;
import net.gliby.voicechat.client.gui.GuiUIPlacementSpeak;
import net.gliby.voicechat.client.gui.GuiUIPlacementVoicePlate;
import net.gliby.voicechat.client.gui.UIPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiUIPlacement
extends GuiScreen {
    private final List<GuiPlaceableInterface> placeables = new ArrayList<GuiPlaceableInterface>();
    private final GuiScreen parent;
    private int offsetX;
    private int offsetY;
    public String[] positionTypes = new String[2];
    private GuiButton positionTypeButton;
    private GuiButton resetButton;
    private GuiBoostSlider scaleSlider;
    private GuiPlaceableInterface selectedUIPlaceable;
    private GuiPlaceableInterface lastSelected;

    public static void drawRectLines(int par0, int par1, int par2, int par3, int par4) {
        int j1;
        if (par0 < par2) {
            j1 = par0;
            par0 = par2;
            par2 = j1;
        }
        if (par1 < par3) {
            j1 = par1;
            par1 = par3;
            par3 = j1;
        }
        float f = (float)(par4 >> 24 & 255) / 255.0f;
        float f1 = (float)(par4 >> 16 & 255) / 255.0f;
        float f2 = (float)(par4 >> 8 & 255) / 255.0f;
        float f3 = (float)(par4 & 255) / 255.0f;
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)f1, (float)f2, (float)f3, (float)f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.startDrawing(2);
        renderer.addVertex((double)par0, (double)par3, 0.0);
        renderer.addVertex((double)par2, (double)par3, 0.0);
        renderer.addVertex((double)par2, (double)par1, 0.0);
        renderer.addVertex((double)par0, (double)par1, 0.0);
        tessellator.draw();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
    }

    public GuiUIPlacement(GuiScreen parent) {
        this.parent = parent;
    }

    public void actionPerformed(GuiButton button) {
        if (button.id == 0 && this.lastSelected != null) {
            this.lastSelected.positionType = this.lastSelected.positionType >= 1 ? 0 : ++this.lastSelected.positionType;
        }
        if (button.id == 1 && this.lastSelected != null) {
            if (this.lastSelected.info.positionType == 0) {
                this.lastSelected.x = this.lastSelected.info.x * (float)this.width;
                this.lastSelected.y = this.lastSelected.info.y * (float)this.height;
            } else {
                this.lastSelected.x = this.lastSelected.info.x;
                this.lastSelected.y = this.lastSelected.info.y;
            }
        }
    }

    public void drawRectWH(int x, int y, int width, int height, int color) {
        GuiUIPlacement.drawRect((int)x, (int)y, (int)(width + x), (int)(height + y), (int)color);
    }

    public void drawScreen(int x, int y, float tick) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format((String)"menu.pressESCtoReturn", (Object[])new Object[0]), this.width / 2, 2, -1);
        if (this.selectedUIPlaceable != null) {
            this.selectedUIPlaceable.x = x - this.offsetX;
            this.selectedUIPlaceable.y = y - this.offsetY;
            if (!Mouse.isButtonDown((int)0)) {
                this.selectedUIPlaceable = null;
            }
        }
        if (this.lastSelected != null) {
            this.scaleSlider.setDisplayString(I18n.format((String)"menu.scale", (Object[])new Object[0]) + ": " + (int)(this.lastSelected.scale * 100.0f) + "%");
            this.scaleSlider.sliderValue = this.lastSelected.scale;
            boolean rightSide = this.inBounds(this.lastSelected.x + (float)this.lastSelected.width + 151.0f, this.lastSelected.y + 42.0f, this.width, 0.0f, this.width, this.height * 2);
            boolean topSide = this.inBounds(this.lastSelected.x + (float)this.lastSelected.width - 75.0f, this.lastSelected.y, - this.width, - this.height, this.width * 2, this.height);
            boolean bottomSide = this.inBounds(this.lastSelected.x + (float)this.lastSelected.width, this.lastSelected.y + 66.0f, 0.0f, this.height, this.width * 2, this.height);
            this.positionTypeButton.xPosition = (int)(this.lastSelected.x + (float)(rightSide ? -100 : this.lastSelected.width + 2));
            this.positionTypeButton.yPosition = (int)(this.lastSelected.y - (bottomSide ? this.lastSelected.y + 66.0f - (float)this.height : (topSide ? this.lastSelected.y - 0.0f : 0.0f)));
            this.scaleSlider.xPosition = (int)(this.lastSelected.x + (float)(rightSide ? -154 : this.lastSelected.width + 2));
            this.scaleSlider.yPosition = (int)(this.lastSelected.y + 22.0f - (bottomSide ? this.lastSelected.y + 66.0f - (float)this.height : (topSide ? this.lastSelected.y - 0.0f : 0.0f)));
            this.resetButton.xPosition = (int)(this.lastSelected.x + (float)(rightSide ? -100 : this.lastSelected.width + 2));
            this.resetButton.yPosition = (int)(this.lastSelected.y + 44.0f - (bottomSide ? this.lastSelected.y + 66.0f - (float)this.height : (topSide ? this.lastSelected.y - 0.0f : 0.0f)));
            this.positionTypeButton.displayString = I18n.format((String)"menu.position", (Object[])new Object[0]) + ": " + this.positionTypes[this.lastSelected.positionType];
            this.positionTypeButton.drawButton(this.mc, x, y);
            this.resetButton.drawButton(this.mc, x, y);
            this.scaleSlider.drawButton(this.mc, x, y);
            this.lastSelected.scale = this.scaleSlider.sliderValue;
        }
        for (int i = 0; i < this.placeables.size(); ++i) {
            GuiPlaceableInterface placeable = this.placeables.get(i);
            GL11.glPushMatrix();
            GL11.glTranslatef((float)placeable.x, (float)placeable.y, (float)0.0f);
            placeable.draw(this.mc, this, x, y, tick);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glTranslatef((float)placeable.x, (float)placeable.y, (float)0.0f);
            GL11.glLineWidth((float)4.0f);
            GuiUIPlacement.drawRectLines(0, 0, placeable.width, placeable.height, this.selectedUIPlaceable == placeable ? -16711936 : -1);
            GL11.glLineWidth((float)1.0f);
            GL11.glPopMatrix();
        }
    }

    public boolean inBounds(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public void initGui() {
        this.positionTypes[0] = I18n.format((String)"menu.positionAutomatic", (Object[])new Object[0]);
        this.positionTypes[1] = I18n.format((String)"menu.positionAbsolute", (Object[])new Object[0]);
        if (this.scaleSlider == null) {
            this.placeables.add(new GuiUIPlacementSpeak(VoiceChat.getProxyInstance().getSettings().getUIPositionSpeak(), this.width, this.height));
            this.placeables.add(new GuiUIPlacementVoicePlate(VoiceChat.getProxyInstance().getSettings().getUIPositionPlate(), this.width, this.height));
        }
        this.positionTypeButton = new GuiButton(0, 2, 2, 96, 20, "Position: Automatic");
        this.buttonList.add(this.positionTypeButton);
        this.resetButton = new GuiButton(1, 2, 2, 96, 20, I18n.format((String)"menu.resetLocation", (Object[])new Object[0]));
        this.buttonList.add(this.resetButton);
        this.scaleSlider = new GuiBoostSlider(2, 2, 2, "", "Scale: 100%", 0.0f);
        this.buttonList.add(this.scaleSlider);
        for (int i = 0; i < this.placeables.size(); ++i) {
            GuiPlaceableInterface placeableInterface = this.placeables.get(i);
            if (placeableInterface.positionType != 0) continue;
            this.resize(placeableInterface);
        }
    }

    public void keyTyped(char par1, int par2) {
        if (this.lastSelected != null) {
            if (par2 == 200) {
                this.lastSelected.y -= 1.0f;
            }
            if (par2 == 208) {
                this.lastSelected.y += 1.0f;
            }
            if (par2 == 205) {
                this.lastSelected.x += 1.0f;
            }
            if (par2 == 203) {
                this.lastSelected.x -= 1.0f;
            }
        }
        if (par2 == 1) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    public void mouseClicked(int x, int y, int b) {
        if (b == 0) {
            if (this.selectedUIPlaceable == null) {
                for (int i = 0; i < this.placeables.size(); ++i) {
                    GuiPlaceableInterface placeable = this.placeables.get(i);
                    if (!this.inBounds(x, y, placeable.x, placeable.y, placeable.width, placeable.height)) continue;
                    this.offsetX = (int)Math.abs((float)x - placeable.x);
                    this.offsetY = (int)Math.abs((float)y - placeable.y);
                    this.lastSelected = this.selectedUIPlaceable = placeable;
                }
            } else {
                this.selectedUIPlaceable = null;
            }
        }
        try {
            super.mouseClicked(x, y, b);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        this.save();
    }

    private void resize(GuiPlaceableInterface placeable) {
        placeable.update((int)((float)this.width * (placeable.x * 1.0f / (float)placeable.screenWidth)), (int)((float)this.height * (placeable.y * 1.0f / (float)placeable.screenHeight)), this.width, this.height);
    }

    public void save() {
        Settings settings = VoiceChat.getProxyInstance().getSettings();
        for (int i = 0; i < this.placeables.size(); ++i) {
            GuiPlaceableInterface placeable = this.placeables.get(i);
            if (placeable.positionType == 0) {
                placeable.positionUI.x = placeable.x * 1.0f / (float)placeable.screenWidth;
                placeable.positionUI.y = placeable.y * 1.0f / (float)placeable.screenHeight;
            } else {
                placeable.positionUI.x = placeable.x;
                placeable.positionUI.y = placeable.y;
            }
            placeable.positionUI.type = placeable.positionType;
            placeable.positionUI.scale = placeable.scale;
        }
        settings.getConfiguration().save();
    }

    public void updateScreen() {
    }
}


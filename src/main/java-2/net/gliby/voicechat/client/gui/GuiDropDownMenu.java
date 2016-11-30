/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public class GuiDropDownMenu
extends GuiButton {
    String[] array;
    boolean[] mouseOn;
    private final int prevHeight;
    private int amountOfItems = 1;
    public boolean dropDownMenu = false;
    public int selectedInteger;

    public GuiDropDownMenu(int par1, int par2, int par3, int par4, int par5, String par6Str, String[] array) {
        super(par1, par2, par3, par4, par5, par6Str);
        this.prevHeight = this.height;
        this.array = array;
        this.amountOfItems = array.length;
        this.mouseOn = new boolean[this.amountOfItems];
    }

    public GuiDropDownMenu(int par1, int par2, int par3, String par4Str, String[] array) {
        super(par1, par2, par3, par4Str);
        this.prevHeight = this.height;
        this.array = array;
        this.amountOfItems = array.length;
        this.mouseOn = new boolean[this.amountOfItems];
    }

    public void drawButton(Minecraft par1Minecraft, int x, int y) {
        if (this.visible) {
            this.height = this.dropDownMenu && this.array.length != 0 ? this.prevHeight * (this.amountOfItems + 1) : this.prevHeight;
            FontRenderer fontrenderer = par1Minecraft.fontRendererObj;
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
            this.getHoverState(this.hovered);
            int l = 14737632;
            GuiDropDownMenu.drawRect((int)(this.xPosition - 1), (int)(this.yPosition - 1), (int)(this.xPosition + this.width + 1), (int)(this.yPosition + this.height + 1), (int)-6250336);
            GuiDropDownMenu.drawRect((int)this.xPosition, (int)this.yPosition, (int)(this.xPosition + this.width), (int)(this.yPosition + this.height), (int)-16777216);
            GuiDropDownMenu.drawRect((int)(this.xPosition - 1), (int)(this.yPosition + this.prevHeight), (int)(this.xPosition + this.width + 1), (int)(this.yPosition + this.prevHeight + 1), (int)-6250336);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            int u = 242;
            u = this.dropDownMenu && this.array.length != 0 ? 228 : 242;
            if (!this.enabled) {
                l = -6250336;
            }
            this.drawCenteredString(fontrenderer, this.displayString.substring(0, Math.min(this.displayString.length(), 22)), this.xPosition + this.width / 2, this.yPosition + (this.prevHeight - 8) / 2, l);
            GL11.glPushMatrix();
            if (this.dropDownMenu && this.array.length != 0) {
                for (int i = 0; i < this.amountOfItems; ++i) {
                    this.mouseOn[i] = this.inBounds(x, y, this.xPosition, this.yPosition + this.prevHeight * (i + 1), this.width, this.prevHeight);
                    String s = this.array[i].substring(0, Math.min(this.array[i].length(), 26)) + "..";
                    this.drawCenteredString(fontrenderer, s, this.xPosition + this.width / 2, this.yPosition + this.prevHeight * (i + 1) + 7, this.mouseOn[i] ? 16777120 : 14737632);
                }
            }
            GL11.glPopMatrix();
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            IndependentGUITexture.TEXTURES.bindTexture(Minecraft.getMinecraft());
            this.drawTexturedModalRect(this.xPosition + this.width - 15, this.yPosition + 2, u, 0, 14, 14);
        }
    }

    public int getMouseOverInteger() {
        for (int i = 0; i < this.mouseOn.length; ++i) {
            if (!this.mouseOn[i]) continue;
            return i;
        }
        return -1;
    }

    public boolean inBounds(int x, int y, int posX, int posY, int width, int height) {
        return this.enabled && this.visible && x >= posX && y >= posY && x < posX + width && y < posY + height;
    }

    public void setArray(String[] array) {
        this.array = array;
        this.amountOfItems = array.length;
        this.mouseOn = new boolean[this.amountOfItems];
    }

    public void setDisplayString(String s) {
        this.displayString = s;
    }
}


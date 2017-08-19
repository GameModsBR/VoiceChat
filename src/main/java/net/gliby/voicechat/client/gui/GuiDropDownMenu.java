package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public class GuiDropDownMenu extends GuiButton {

    private final int prevHeight;
    public boolean dropDownMenu = false;
    public int selectedInteger;
    String[] array;
    boolean[] mouseOn;
    private int amountOfItems = 1;


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

    @Override
    public void drawButton(Minecraft par1Minecraft, int x, int y, float partialTicks) {
        if (this.visible) {
            if (this.dropDownMenu && this.array.length != 0) {
                this.height = this.prevHeight * (this.amountOfItems + 1);
            } else {
                this.height = this.prevHeight;
            }

            FontRenderer fontrenderer = par1Minecraft.fontRenderer;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
            this.getHoverState(this.hovered);
            int l = 14737632;
            drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            drawRect(this.x - 1, this.y + this.prevHeight, this.x + this.width + 1, this.y + this.prevHeight + 1, -6250336);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean u = true;
            short var9;
            if (this.dropDownMenu && this.array.length != 0) {
                var9 = 228;
            } else {
                var9 = 242;
            }

            if (!this.enabled) {
                l = -6250336;
            }

            this.drawCenteredString(fontrenderer, this.displayString.substring(0, Math.min(this.displayString.length(), 22)), this.x + this.width / 2, this.y + (this.prevHeight - 8) / 2, l);
            GL11.glPushMatrix();
            if (this.dropDownMenu && this.array.length != 0) {
                for (int i = 0; i < this.amountOfItems; ++i) {
                    this.mouseOn[i] = this.inBounds(x, y, this.x, this.y + this.prevHeight * (i + 1), this.width, this.prevHeight);
                    String s = this.array[i].substring(0, Math.min(this.array[i].length(), 26)) + "..";
                    this.drawCenteredString(fontrenderer, s, this.x + this.width / 2, this.y + this.prevHeight * (i + 1) + 7, this.mouseOn[i] ? 16777120 : 14737632);
                }
            }

            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            IndependentGUITexture.TEXTURES.bindTexture(Minecraft.getMinecraft());
            this.drawTexturedModalRect(this.x + this.width - 15, this.y + 2, var9, 0, 14, 14);
        }

    }

    public int getMouseOverInteger() {
        for (int i = 0; i < this.mouseOn.length; ++i) {
            if (this.mouseOn[i]) {
                return i;
            }
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

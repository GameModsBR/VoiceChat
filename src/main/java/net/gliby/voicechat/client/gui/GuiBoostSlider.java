package net.gliby.voicechat.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiBoostSlider extends GuiButton {

    public float sliderValue = 1.0F;
    public boolean dragging;
    public String idValue;


    public GuiBoostSlider(int par1, int par2, int par3, String idValue, String par5Str, float par6) {
        super(par1, par2, par3, 150, 20, par5Str);
        this.idValue = idValue;
        this.sliderValue = par6;
    }

    @Override
    public int getHoverState(boolean par1) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);
                if (this.sliderValue < 0.0F) {
                    this.sliderValue = 0.0F;
                }

                if (this.sliderValue > 1.0F) {
                    this.sliderValue = 1.0F;
                }

                this.displayString = this.idValue;
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }

    }

    @Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
        if (super.mousePressed(par1Minecraft, par2, par3)) {
            this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);
            if (this.sliderValue < 0.0F) {
                this.sliderValue = 0.0F;
            }

            if (this.sliderValue > 1.0F) {
                this.sliderValue = 1.0F;
            }

            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int par1, int par2) {
        this.dragging = false;
    }

    public void setDisplayString(String display) {
        this.idValue = display;
        this.displayString = display;
    }
}

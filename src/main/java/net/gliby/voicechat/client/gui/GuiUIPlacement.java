package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.Settings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiUIPlacement extends GuiScreen {

    private final List<GuiPlaceableInterface> placeables = new ArrayList<GuiPlaceableInterface>();
    private final GuiScreen parent;
    public String[] positionTypes = new String[2];
    private int offsetX;
    private int offsetY;
    private GuiButton positionTypeButton;
    private GuiButton resetButton;
    private GuiBoostSlider scaleSlider;
    private GuiPlaceableInterface selectedUIPlaceable;
    private GuiPlaceableInterface lastSelected;


    public GuiUIPlacement(GuiScreen parent) {
        this.parent = parent;
    }

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

        float f = (float) (par4 >> 24 & 255) / 255.0F;
        float f1 = (float) (par4 >> 16 & 255) / 255.0F;
        float f2 = (float) (par4 >> 8 & 255) / 255.0F;
        float f3 = (float) (par4 & 255) / 255.0F;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(f1, f2, f3, f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();
        renderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        renderer.pos((double) par0, (double) par3, 0.0D).endVertex();
        renderer.pos((double) par2, (double) par3, 0.0D).endVertex();
        renderer.pos((double) par2, (double) par1, 0.0D).endVertex();
        renderer.pos((double) par0, (double) par1, 0.0D).endVertex();
        tessellator.draw();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0 && this.lastSelected != null) {
            if (this.lastSelected.positionType >= 1) {
                this.lastSelected.positionType = 0;
            } else {
                ++this.lastSelected.positionType;
            }
        }

        if (button.id == 1 && this.lastSelected != null) {
            if (this.lastSelected.info.positionType == 0) {
                this.lastSelected.x = this.lastSelected.info.x * (float) this.width;
                this.lastSelected.y = this.lastSelected.info.y * (float) this.height;
            } else {
                this.lastSelected.x = this.lastSelected.info.x;
                this.lastSelected.y = this.lastSelected.info.y;
            }
        }

    }

    public void drawRectWH(int x, int y, int width, int height, int color) {
        drawRect(x, y, width + x, height + y, color);
    }

    @Override
    public void drawScreen(int x, int y, float tick) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("menu.pressESCtoReturn"), this.width / 2, 2, -1);
        if (this.selectedUIPlaceable != null) {
            this.selectedUIPlaceable.x = (float) (x - this.offsetX);
            this.selectedUIPlaceable.y = (float) (y - this.offsetY);
            if (!Mouse.isButtonDown(0)) {
                this.selectedUIPlaceable = null;
            }
        }

        if (this.lastSelected != null) {
            this.scaleSlider.setDisplayString(I18n.format("menu.scale") + ": " + (int) (this.lastSelected.scale * 100.0F) + "%");
            this.scaleSlider.sliderValue = this.lastSelected.scale;
            boolean i = this.inBounds(this.lastSelected.x + (float) this.lastSelected.width + 151.0F, this.lastSelected.y + 42.0F, (float) this.width, 0.0F, (float) this.width, (float) (this.height * 2));
            boolean placeable = this.inBounds(this.lastSelected.x + (float) this.lastSelected.width - 75.0F, this.lastSelected.y, (float) (-this.width), (float) (-this.height), (float) (this.width * 2), (float) this.height);
            boolean bottomSide = this.inBounds(this.lastSelected.x + (float) this.lastSelected.width, this.lastSelected.y + 66.0F, 0.0F, (float) this.height, (float) (this.width * 2), (float) this.height);
            this.positionTypeButton.x = (int) (this.lastSelected.x + (float) (i ? -100 : this.lastSelected.width + 2));
            this.positionTypeButton.y = (int) (this.lastSelected.y - (bottomSide ? this.lastSelected.y + 66.0F - (float) this.height : (placeable ? this.lastSelected.y - 0.0F : 0.0F)));
            this.scaleSlider.x = (int) (this.lastSelected.x + (float) (i ? -154 : this.lastSelected.width + 2));
            this.scaleSlider.y = (int) (this.lastSelected.y + 22.0F - (bottomSide ? this.lastSelected.y + 66.0F - (float) this.height : (placeable ? this.lastSelected.y - 0.0F : 0.0F)));
            this.resetButton.x = (int) (this.lastSelected.x + (float) (i ? -100 : this.lastSelected.width + 2));
            this.resetButton.y = (int) (this.lastSelected.y + 44.0F - (bottomSide ? this.lastSelected.y + 66.0F - (float) this.height : (placeable ? this.lastSelected.y - 0.0F : 0.0F)));
            this.positionTypeButton.displayString = I18n.format("menu.position") + ": " + this.positionTypes[this.lastSelected.positionType];
            this.positionTypeButton.drawButton(this.mc, x, y, 0);
            this.resetButton.drawButton(this.mc, x, y, 0);
            this.scaleSlider.drawButton(this.mc, x, y, 0);
            this.lastSelected.scale = this.scaleSlider.sliderValue;
        }

        for (int var7 = 0; var7 < this.placeables.size(); ++var7) {
            GuiPlaceableInterface var8 = this.placeables.get(var7);
            GL11.glPushMatrix();
            GL11.glTranslatef(var8.x, var8.y, 0.0F);
            var8.draw(this.mc, this, x, y, tick);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glTranslatef(var8.x, var8.y, 0.0F);
            GL11.glLineWidth(4.0F);
            drawRectLines(0, 0, var8.width, var8.height, this.selectedUIPlaceable == var8 ? -16711936 : -1);
            GL11.glLineWidth(1.0F);
            GL11.glPopMatrix();
        }

    }

    public boolean inBounds(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    public void initGui() {
        this.positionTypes[0] = I18n.format("menu.positionAutomatic");
        this.positionTypes[1] = I18n.format("menu.positionAbsolute");
        if (this.scaleSlider == null) {
            this.placeables.add(new GuiUIPlacementSpeak(VoiceChat.getProxyInstance().getSettings().getUIPositionSpeak(), this.width, this.height));
            this.placeables.add(new GuiUIPlacementVoicePlate(VoiceChat.getProxyInstance().getSettings().getUIPositionPlate(), this.width, this.height));
        }

        buttonList.add(this.positionTypeButton = new GuiButton(0, 2, 2, 96, 20, "Position: Automatic"));
        buttonList.add(this.resetButton = new GuiButton(1, 2, 2, 96, 20, I18n.format("menu.resetLocation")));
        buttonList.add(this.scaleSlider = new GuiBoostSlider(2, 2, 2, "", "Scale: 100%", 0.0F));

        for (int i = 0; i < this.placeables.size(); ++i) {
            GuiPlaceableInterface placeableInterface = this.placeables.get(i);
            if (placeableInterface.positionType == 0) {
                this.resize(placeableInterface);
            }
        }

    }

    @Override
    public void keyTyped(char par1, int par2) {
        if (this.lastSelected != null) {
            if (par2 == 200) {
                --this.lastSelected.y;
            }

            if (par2 == 208) {
                ++this.lastSelected.y;
            }

            if (par2 == 205) {
                ++this.lastSelected.x;
            }

            if (par2 == 203) {
                --this.lastSelected.x;
            }
        }

        if (par2 == 1) {
            this.mc.displayGuiScreen(this.parent);
        }

    }

    @Override
    public void mouseClicked(int x, int y, int b) {
        if (b == 0) {
            if (this.selectedUIPlaceable == null) {
                for (int e = 0; e < this.placeables.size(); ++e) {
                    GuiPlaceableInterface placeable = this.placeables.get(e);
                    if (this.inBounds((float) x, (float) y, placeable.x, placeable.y, (float) placeable.width, (float) placeable.height)) {
                        this.offsetX = (int) Math.abs((float) x - placeable.x);
                        this.offsetY = (int) Math.abs((float) y - placeable.y);
                        this.selectedUIPlaceable = placeable;
                        this.lastSelected = this.selectedUIPlaceable;
                    }
                }
            } else {
                this.selectedUIPlaceable = null;
            }
        }

        try {
            super.mouseClicked(x, y, b);
        } catch (IOException var6) {
            var6.printStackTrace();
        }

    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.save();
    }

    private void resize(GuiPlaceableInterface placeable) {
        placeable.update((int) ((float) this.width * (placeable.x * 1.0F / (float) placeable.screenWidth)), (int) ((float) this.height * (placeable.y * 1.0F / (float) placeable.screenHeight)), this.width, this.height);
    }

    public void save() {
        Settings settings = VoiceChat.getProxyInstance().getSettings();

        for (int i = 0; i < this.placeables.size(); ++i) {
            GuiPlaceableInterface placeable = this.placeables.get(i);
            if (placeable.positionType == 0) {
                placeable.positionUI.x = placeable.x * 1.0F / (float) placeable.screenWidth;
                placeable.positionUI.y = placeable.y * 1.0F / (float) placeable.screenHeight;
            } else {
                placeable.positionUI.x = placeable.x;
                placeable.positionUI.y = placeable.y;
            }

            placeable.positionUI.type = placeable.positionType;
            placeable.positionUI.scale = placeable.scale;
        }

        settings.getConfiguration().save();
    }

    @Override
    public void updateScreen() {
    }
}

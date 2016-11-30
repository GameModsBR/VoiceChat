/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.client.gui;

public enum EnumUIPlacement {
    VOICE_PLATES(0.84f, 0.0078f, 3, 2, 0),
    SPEAK(0.86f, 0.74f, 1, 3, 0);
    
    int offsetX;
    int offsetY;
    public int positionType;
    public float x;
    public float y;

    private EnumUIPlacement(float x, float y, int offsetX, int offsetY, int positionType) {
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.positionType = positionType;
    }
}


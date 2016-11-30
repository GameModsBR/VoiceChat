package net.gliby.voicechat.client.keybindings;


public enum EnumBinding {

    SPEAK("SPEAK", 0, "Speak"),
    OPEN_GUI_OPTIONS("OPEN_GUI_OPTIONS", 1, "Gliby\'s Options Menu");
    // $FF: synthetic field
    private static final EnumBinding[] $VALUES = new EnumBinding[]{SPEAK, OPEN_GUI_OPTIONS};
    public String name;


    EnumBinding(String var1, int var2, String name) {
        this.name = name;
    }

}

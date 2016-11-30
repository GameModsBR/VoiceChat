/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.audio.SoundHandler
 *  net.minecraft.client.audio.SoundManager
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.fml.common.FMLCommonHandler
 *  net.minecraftforge.fml.common.ModMetadata
 *  net.minecraftforge.fml.common.event.FMLInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLPreInitializationEvent
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 *  org.apache.logging.log4j.Logger
 */
package net.gliby.voicechat.client;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.UpdatedSoundManager;
import net.gliby.voicechat.client.debug.Statistics;
import net.gliby.voicechat.client.gui.GuiInGameHandlerVoiceChat;
import net.gliby.voicechat.client.keybindings.KeyManager;
import net.gliby.voicechat.client.keybindings.KeyTickHandler;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.client.networking.game.ClientDisconnectHandler;
import net.gliby.voicechat.client.networking.game.ClientEventHandler;
import net.gliby.voicechat.client.render.RenderPlayerVoiceIcon;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.Recorder;
import net.gliby.voicechat.client.sound.SoundSystemWrapper;
import net.gliby.voicechat.common.VoiceChatServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

public class VoiceChatClient
extends VoiceChatServer {
    @SideOnly(value=Side.CLIENT)
    private static ClientStreamManager soundManager;
    @SideOnly(value=Side.CLIENT)
    private static Statistics stats;
    public static ModMetadata modMetadata;
    @SideOnly(value=Side.CLIENT)
    private File configurationDirectory;
    @SideOnly(value=Side.CLIENT)
    private Settings settings;
    @SideOnly(value=Side.CLIENT)
    public KeyManager keyManager;
    @SideOnly(value=Side.CLIENT)
    private ClientNetwork clientNetwork;
    @SideOnly(value=Side.CLIENT)
    private boolean recorderActive;
    @SideOnly(value=Side.CLIENT)
    public SoundSystemWrapper sndSystem;
    @SideOnly(value=Side.CLIENT)
    private VoiceChat voiceChat;
    public Recorder recorder;
    public Map<String, Integer> specialPlayers = new HashMap<String, Integer>();
    String[] testPlayers = new String[]{"captaindogfish", "starguy1245", "SheheryaB", "arsham123", "Chris9awesome", "TechnoX_X", "bubz052", "McJackson3180", "InfamousArgyle", "jdf2", "XxNotexX0", "SirDenerim", "Frankspark", "smith70831", "killazombiecow", "CraftAeternalis", "choclaterainxx", "dragonballkid4", "TH3_CR33PER", "yetshadow", "KristinnVikarJ", "TheMCBros99", "kevinlame"};

    public static synchronized Logger getLogger() {
        return LOGGER;
    }

    public static ModMetadata getModMetadata() {
        return modMetadata;
    }

    public static final ClientStreamManager getSoundManager() {
        return soundManager;
    }

    public static final Statistics getStatistics() {
        return stats;
    }

    public ClientNetwork getClientNetwork() {
        return this.clientNetwork;
    }

    private SoundManager getMinecraftSoundManager(Minecraft mc) {
        try {
            Field field = SoundHandler.class.getDeclaredFields()[5];
            field.setAccessible(true);
            SoundManager soundManager = (SoundManager)field.get((Object)mc.getSoundHandler());
            return soundManager;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Settings getSettings() {
        return this.settings;
    }

    public String[] getTestPlayers() {
        return this.testPlayers;
    }

    @Override
    public void initMod(VoiceChat voiceChat, FMLInitializationEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        new UpdatedSoundManager(this, this.getMinecraftSoundManager(mc)).init(event);
        this.voiceChat = voiceChat;
        this.recorder = new Recorder(this);
        this.keyManager.init();
        if (this.settings.isDebug()) {
            VoiceChatClient.getLogger().info("Debug enabled!");
            stats = new Statistics();
        }
        VoiceChatClient.getLogger().info("Started client-side on version (" + this.getVersion() + ")" + "");
        this.clientNetwork = new ClientNetwork(this);
        MinecraftForge.EVENT_BUS.register((Object)new GuiInGameHandlerVoiceChat(this));
        MinecraftForge.EVENT_BUS.register((Object)new RenderPlayerVoiceIcon(this, mc));
        this.sndSystem = new SoundSystemWrapper(mc.getSoundHandler());
        MinecraftForge.EVENT_BUS.register((Object)this.sndSystem);
        MinecraftForge.EVENT_BUS.register((Object)new ClientEventHandler(this));
        FMLCommonHandler.instance().bus().register((Object)new ClientDisconnectHandler());
        FMLCommonHandler.instance().bus().register((Object)new KeyTickHandler(this));
        VoiceChatClient.getLogger().info("Created SoundSystemWrapper: " + this.sndSystem + ".");
    }

    public final boolean isRecorderActive() {
        return this.recorderActive;
    }

    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        modMetadata = event.getModMetadata();
        this.configurationDirectory = new File(event.getModConfigurationDirectory(), "gliby_vc");
        if (!this.configurationDirectory.exists()) {
            this.configurationDirectory.mkdir();
        }
        this.settings = new Settings(new File(this.configurationDirectory, "ClientSettings.ini"));
        this.settings.init();
        this.keyManager = new KeyManager(this);
        this.specialPlayers.put("theGliby", 1);
        this.specialPlayers.put("Rinto", 1);
        this.specialPlayers.put("DanielSturk", 1);
        this.specialPlayers.put("CraftAeternalis", 3);
        this.specialPlayers.put("YETSHADOW", 5);
        this.specialPlayers.put("McJackson3180", 6);
        this.specialPlayers.put("smith70831", 7);
        this.specialPlayers.put("XxNotexX0", 8);
        this.specialPlayers.put("TheHaxman2", 9);
        soundManager = new ClientStreamManager(Minecraft.getMinecraft(), this);
        soundManager.init();
    }

    public void setRecorderActive(boolean b) {
        if (this.clientNetwork.voiceClientExists()) {
            this.recorderActive = b;
        }
    }
}


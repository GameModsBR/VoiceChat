/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  net.minecraft.command.ICommand
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.dedicated.DedicatedServer
 *  net.minecraftforge.fml.common.FMLCommonHandler
 *  net.minecraftforge.fml.common.ModMetadata
 *  net.minecraftforge.fml.common.event.FMLInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLPostInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLPreInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent
 *  net.minecraftforge.fml.common.event.FMLServerStartedEvent
 *  net.minecraftforge.fml.common.event.FMLServerStartingEvent
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.gliby.voicechat.common;

import net.gliby.gman.GMan;
import net.gliby.gman.ModInfo;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.api.VoiceChatAPI;
import net.gliby.voicechat.common.commands.CommandChatMode;
import net.gliby.voicechat.common.commands.CommandVoiceMute;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.gliby.voicechat.common.networking.voiceservers.MinecraftVoiceServer;
import net.gliby.voicechat.common.networking.voiceservers.ServerConnectionHandler;
import net.gliby.voicechat.common.networking.voiceservers.VoiceAuthenticatedServer;
import net.gliby.voicechat.common.networking.voiceservers.VoiceServer;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPVoiceServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.Executors;

public class VoiceChatServer {
    public static final String VERSION = "0.6.1";
    protected static final Logger LOGGER = LogManager.getLogger((String) "Gliby's Voice Chat Mod");
    private static final String MC_VERSION = "1.8";
    public ModInfo modInfo;
    public ServerNetwork serverNetwork;
    public ServerSettings serverSettings;
    private VoiceServer voiceServer;
    private Thread voiceServerThread;
    private File configurationDirectory;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean available(int port) {
        if (port < 4000 || port > 65535) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static synchronized Logger getLogger() {
        return LOGGER;
    }

    public static String getMinecraftVersion() {
        return "1.8";
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

    public void commonInit(final FMLPreInitializationEvent event) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {

            @Override
            public void run() {
                VoiceChatServer.this.modInfo = new ModInfo("gvc", event.getModMetadata().updateUrl);
                GMan.launchMod(VoiceChatServer.getLogger(), VoiceChatServer.this.modInfo, VoiceChatServer.getMinecraftVersion(), VoiceChatServer.this.getVersion());
            }
        });
        new VoiceChatAPI().init();
    }

    private int getAvailablePort() throws IOException {
        int port = 0;
        while (!VoiceChatServer.available(port = VoiceChatServer.randInt(4001, 65534))) {
        }
        return port;
    }

    public ModInfo getModInfo() {
        return this.modInfo;
    }

    private int getNearestPort(int port) {
        return ++port;
    }

    public synchronized ServerNetwork getServerNetwork() {
        return this.serverNetwork;
    }

    public ServerSettings getServerSettings() {
        return this.serverSettings;
    }

    public String getVersion() {
        return "0.6.1";
    }

    public VoiceServer getVoiceServer() {
        return this.voiceServer;
    }

    public void initMod(VoiceChat voiceChat, FMLInitializationEvent event) {
    }

    public void initServer(FMLServerStartedEvent event) {
        MinecraftServer server = MinecraftServer.getServer();
        if (this.serverSettings.getUDPPort() == 0) {
            if (server.isDedicatedServer()) {
                int queryPort = -1;
                if (((DedicatedServer) server).getBooleanProperty("enable-query", false)) {
                    queryPort = ((DedicatedServer) server).getIntProperty("query.port", 0);
                }
                boolean portTaken = queryPort == server.getServerPort();
                this.serverSettings.setUDPPort(portTaken ? this.getNearestPort(server.getServerPort()) : server.getServerPort());
                if (portTaken) {
                    VoiceChatServer.getLogger().warn("Hey! Over Here! It seems you are running a query on the default port. We can't run a voice server on this port, so I've found a new one just for you! I'd recommend changing the UDPPort in your configuration, if the voice server can't bind!");
                }
            } else {
                try {
                    this.serverSettings.setUDPPort(this.getAvailablePort());
                } catch (IOException e) {
                    VoiceChatServer.getLogger().fatal("Couldn't start voice server.");
                    e.printStackTrace();
                    return;
                }
            }
        }
        this.voiceServerThread = this.startVoiceServer();
    }

    public void postInitMod(VoiceChat voiceChat, FMLPostInitializationEvent event) {
    }

    public void preInitClient(FMLPreInitializationEvent event) {
    }

    public void aboutToStartServer(FMLServerAboutToStartEvent event) {
        FMLCommonHandler.instance().bus().register(new ServerConnectionHandler(this));
        this.serverSettings = new ServerSettings(this);
        this.configurationDirectory = new File("config/gliby_vc");
        if (!this.configurationDirectory.exists()) {
            this.configurationDirectory.mkdir();
        }
        this.serverSettings.preInit(new File(this.configurationDirectory, "ServerSettings.ini"));
    }

    public void preInitServer(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandVoiceMute());
        event.registerServerCommand(new CommandChatMode());
    }

    private Thread startVoiceServer() {
        this.serverNetwork = new ServerNetwork(this);
        this.serverNetwork.init();
        switch (this.serverSettings.getAdvancedNetworkType()) {
            case 0: {
                this.voiceServer = new MinecraftVoiceServer(this);
                break;
            }
            case 1: {
                this.voiceServer = new UDPVoiceServer(this);
                break;
            }
            default: {
                this.voiceServer = new MinecraftVoiceServer(this);
            }
        }
        Thread thread = new Thread(this.voiceServer, "Voice Server Process");
        thread.setDaemon(this.voiceServer instanceof VoiceAuthenticatedServer);
        thread.start();
        return thread;
    }

    public void stop() {
        this.serverNetwork.stop();
        if (this.voiceServer instanceof VoiceAuthenticatedServer) {
            ((VoiceAuthenticatedServer) this.voiceServer).waitingAuth.clear();
        }
        this.voiceServer.stop();
        this.voiceServer = null;
        this.voiceServerThread.stop();
    }

}


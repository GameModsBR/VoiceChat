/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Logger
 */
package net.gliby.voicechat.common.networking.voiceservers.udp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class UdpServer {
    private static Logger LOGGER;
    public static final String PORT_PROP = "port";
    private static final int PORT_DEFAULT = 8000;
    public static final String GROUPS_PROP = "groups";
    private static final String GROUPS_DEFAULT = null;
    public static final String STATE_PROP = "state";
    public static final String LAST_EXCEPTION_PROP = "lastException";
    private int port = 8000;
    private String groups = GROUPS_DEFAULT;
    private State currentState = State.STOPPED;
    private final Collection<Listener> listeners = new LinkedList<Listener>();
    private final Event event;
    private final PropertyChangeSupport propSupport;
    private final UdpServer This;
    private ThreadFactory threadFactory;
    private Thread ioThread;
    private MulticastSocket mSocket;
    private final DatagramPacket packet;
    private Throwable lastException;
    String hostname;

    public UdpServer(Logger logger) {
        this.event = new Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger;
    }

    public UdpServer(Logger logger, int port, ThreadFactory factory) {
        this.event = new Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger;
        this.port = port;
        this.threadFactory = factory;
    }

    public UdpServer(Logger logger2, String hostname, int port) {
        this.event = new Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger2;
        this.port = port;
        this.hostname = hostname;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propSupport.addPropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.propSupport.addPropertyChangeListener(property, listener);
    }

    public synchronized void addUdpServerListener(Listener l) {
        this.listeners.add(l);
    }

    public void clearUdpListeners() {
        this.listeners.clear();
    }

    protected void fireExceptionNotification(Throwable t) {
        Throwable oldVal = this.lastException;
        this.lastException = t;
        this.firePropertyChange("lastException", oldVal, t);
    }

    public synchronized void fireProperties() {
        this.firePropertyChange("port", null, this.getPort());
        this.firePropertyChange("groups", null, this.getGroups());
        this.firePropertyChange("state", null, (Object)this.getState());
    }

    protected synchronized void firePropertyChange(String prop, Object oldVal, Object newVal) {
        try {
            this.propSupport.firePropertyChange(prop, oldVal, newVal);
        }
        catch (Exception exc) {
            LOGGER.log(Level.WARN, "A property change listener threw an exception: " + exc.getMessage(), (Throwable)exc);
            this.fireExceptionNotification(exc);
        }
    }

    protected synchronized void fireUdpServerPacketReceived() {
        Listener[] ll;
        for (Listener l : ll = this.listeners.toArray(new Listener[this.listeners.size()])) {
            try {
                l.packetReceived(this.event);
                continue;
            }
            catch (Exception exc) {
                LOGGER.warn("UdpServer.Listener " + l + " threw an exception: " + exc.getMessage());
                this.fireExceptionNotification(exc);
            }
        }
    }

    public synchronized String getGroups() {
        return this.groups;
    }

    public synchronized Throwable getLastException() {
        return this.lastException;
    }

    public synchronized DatagramPacket getPacket() {
        return this.packet;
    }

    public synchronized int getPort() {
        return this.port;
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        if (this.mSocket == null) {
            throw new SocketException("getReceiveBufferSize() cannot be called when the server is not started.");
        }
        return this.mSocket.getReceiveBufferSize();
    }

    public synchronized State getState() {
        return this.currentState;
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propSupport.removePropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        this.propSupport.removePropertyChangeListener(property, listener);
    }

    public synchronized void removeUdpServerListener(Listener l) {
        this.listeners.remove(l);
    }

    public synchronized void reset() {
        switch (this.currentState) {
            case STARTED: {
                this.addPropertyChangeListener("state", new PropertyChangeListener(){

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        State newState = (State)((Object)evt.getNewValue());
                        if (newState == State.STOPPED) {
                            UdpServer server = (UdpServer)evt.getSource();
                            server.removePropertyChangeListener("state", this);
                            server.start();
                        }
                    }
                });
                this.stop();
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void runServer() {
        try {
            InetAddress addr = InetAddress.getByName(this.hostname);
            this.mSocket = new MulticastSocket(new InetSocketAddress(addr, this.getPort()));
            LOGGER.info("UDP Server established on port hostname(" + this.hostname + ") " + this.getPort());
            try {
                this.mSocket.setReceiveBufferSize(this.packet.getData().length);
                LOGGER.info("UDP Server receive buffer size (bytes): " + this.mSocket.getReceiveBufferSize());
            }
            catch (IOException exc) {
                int pl = this.packet.getData().length;
                int bl = this.mSocket.getReceiveBufferSize();
                LOGGER.warn(String.format("Could not set receive buffer to %d. It is now at %d. Error: %s", pl, bl, exc.getMessage()));
            }
            String gg = this.getGroups();
            if (gg != null) {
                String[] proposed;
                for (String p : proposed = gg.split("[\\s,]+")) {
                    try {
                        this.mSocket.joinGroup(InetAddress.getByName(p));
                        LOGGER.info("UDP Server joined multicast group " + p);
                        continue;
                    }
                    catch (IOException exc) {
                        LOGGER.warn("Could not join " + p + " as a multicast group: " + exc.getMessage());
                    }
                }
            }
            this.setState(State.STARTED);
            LOGGER.info("UDP Server listening...");
            while (!this.mSocket.isClosed()) {
                UdpServer proposed = this.This;
                synchronized (proposed) {
                    if (this.currentState == State.STOPPING) {
                        LOGGER.info("Stopping UDP Server by request.");
                        this.mSocket.close();
                    }
                }
                if (this.mSocket.isClosed()) continue;
                this.mSocket.receive(this.packet);
                this.fireUdpServerPacketReceived();
            }
        }
        catch (Exception exc) {
            UdpServer gg = this.This;
            synchronized (gg) {
                if (this.currentState == State.STOPPING) {
                    this.mSocket.close();
                    LOGGER.info("Udp Server closed normally.");
                } else {
                    LOGGER.warn("If the server cannot bind: Switch to Minecraft Networking in config or setup UDP properly, that means port-forwarding.");
                    LOGGER.log(Level.WARN, "Server closed unexpectedly: " + exc.getMessage(), (Throwable)exc);
                }
            }
            this.fireExceptionNotification(exc);
        }
        finally {
            this.setState(State.STOPPING);
            if (this.mSocket != null) {
                this.mSocket.close();
            }
            this.mSocket = null;
        }
    }

    public synchronized void send(DatagramPacket packet) throws IOException {
        if (this.mSocket == null) {
            throw new IOException("No socket available to send packet; is the server running?");
        }
        this.mSocket.send(packet);
    }

    public synchronized void setGroups(String group) {
        String oldVal = this.groups;
        this.groups = group;
        if (this.getState() == State.STARTED) {
            this.reset();
        }
        this.firePropertyChange("groups", oldVal, this.groups);
    }

    public synchronized void setPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Cannot set port outside range 0..65535: " + port);
        }
        int oldVal = this.port;
        this.port = port;
        if (this.getState() == State.STARTED) {
            this.reset();
        }
        this.firePropertyChange("port", oldVal, port);
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (this.mSocket == null) {
            throw new SocketException("setReceiveBufferSize(..) cannot be called when the server is not started.");
        }
        this.mSocket.setReceiveBufferSize(size);
    }

    protected synchronized void setState(State state) {
        State oldVal = this.currentState;
        this.currentState = state;
        this.firePropertyChange("state", (Object)oldVal, (Object)state);
    }

    public synchronized void start() {
        if (this.currentState == State.STOPPED) {
            assert (this.ioThread == null);
            Runnable run = new Runnable(){

                @Override
                public void run() {
                    UdpServer.this.runServer();
                    UdpServer.this.ioThread = null;
                    UdpServer.this.setState(State.STOPPED);
                }
            };
            this.ioThread = this.threadFactory != null ? this.threadFactory.newThread(run) : new Thread(run, this.getClass().getName());
            this.setState(State.STARTING);
            this.ioThread.start();
        }
    }

    public synchronized void stop() {
        if (this.currentState == State.STARTED) {
            this.setState(State.STOPPING);
            if (this.mSocket != null) {
                this.mSocket.close();
            }
        }
    }

    public enum State {
        STARTING,
        STARTED,
        STOPPING,
        STOPPED;


        State() {
        }
    }

    public interface Listener
            extends EventListener {
        void packetReceived(Event var1);
    }

    public static class Event
            extends EventObject {
        private static final long serialVersionUID = 1;

        public Event(UdpServer src) {
            super(src);
        }

        public DatagramPacket getPacket() {
            return this.getUdpServer().getPacket();
        }

        public byte[] getPacketAsBytes() {
            DatagramPacket packet = this.getPacket();
            if (packet == null) {
                return null;
            }
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);
            return data;
        }

        public String getPacketAsString() {
            DatagramPacket packet = this.getPacket();
            if (packet == null) {
                return null;
            }
            String s = new String(packet.getData(), packet.getOffset(), packet.getLength());
            return s;
        }

        public State getState() {
            return this.getUdpServer().getState();
        }

        public UdpServer getUdpServer() {
            return (UdpServer)this.getSource();
        }

        public void send(DatagramPacket packet) throws IOException {
            this.getUdpServer().send(packet);
        }
    }

}


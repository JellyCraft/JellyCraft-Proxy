package net.md_5.bungee.rcon;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.rcon.server.RconServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;

public class RCON {
    public static RCON instance;
    private static RconServer rconServer;

    public static void init(int port, String password) {
        final SocketAddress address = new InetSocketAddress(port);
        ProxyServer.getInstance().getLogger().log(Level.INFO, "Binding RCON to address: {0}...", address);
        rconServer = new RconServer(ProxyServer.getInstance(), password);
        final ChannelFuture future = rconServer.bind(address);
        final Channel channel = future.awaitUninterruptibly().channel();
        if (!channel.isActive()) {
            ProxyServer.getInstance().getLogger().warning("Failed to bind RCON port. Address already in use?");
        }
    }

    public static void destroy() {
        if (rconServer != null) {
            ProxyServer.getInstance().getLogger().info("Stopping RCON listener");
            rconServer.shutdown();
        }
    }
}

package net.md_5.bungee.rcon.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.md_5.bungee.api.ProxyServer;

import java.net.SocketAddress;

public class RconServer
{
    private final ProxyServer server;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    
    public ProxyServer getServer() {
        return this.server;
    }
    
    public RconServer(final ProxyServer server, final String password) {
        this.bootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.server = server;
        this.bootstrap.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(final SocketChannel ch) {
                ch.pipeline().addLast(new RconFramingHandler()).addLast(new RconHandler(RconServer.this, password));
            }
        });
    }
    
    public ChannelFuture bind(final SocketAddress address) {
        return this.bootstrap.bind(address);
    }
    
    public void shutdown() {
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }
}

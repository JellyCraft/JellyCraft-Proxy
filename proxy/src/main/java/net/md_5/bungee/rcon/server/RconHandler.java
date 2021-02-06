package net.md_5.bungee.rcon.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.rcon.RconCommandSender;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class RconHandler extends SimpleChannelInboundHandler<ByteBuf>
{
    private static final byte FAILURE = -1;
    private static final byte TYPE_RESPONSE = 0;
    private static final byte TYPE_COMMAND = 2;
    private static final byte TYPE_LOGIN = 3;
    private final String password;
    private boolean loggedIn;
    private final RconServer rconServer;
    private final RconCommandSender commandSender;
    
    public RconHandler(final RconServer rconServer, final String password) {
        this.loggedIn = false;
        this.rconServer = rconServer;
        this.password = password;
        this.commandSender = new RconCommandSender(rconServer.getServer());
    }
    
    protected void channelRead0(final ChannelHandlerContext ctx, ByteBuf buf) {
        buf = buf.order(ByteOrder.LITTLE_ENDIAN);
        if (buf.readableBytes() < 8) {
            return;
        }
        final int requestId = buf.readInt();
        final int type = buf.readInt();
        final byte[] payloadData = new byte[buf.readableBytes() - 2];
        buf.readBytes(payloadData);
        final String payload = new String(payloadData, StandardCharsets.UTF_8);
        buf.readBytes(2);
        if (type == 3) {
            this.handleLogin(ctx, payload, requestId);
        }
        else if (type == 2) {
            this.handleCommand(ctx, payload, requestId);
        }
        else {
            this.sendLargeResponse(ctx, requestId, "Unknown request " + Integer.toHexString(type));
        }
    }
    
    private void handleLogin(final ChannelHandlerContext ctx, final String payload, final int requestId) {
        if (this.password.equals(payload)) {
            this.loggedIn = true;
            this.sendResponse(ctx, requestId, 2, "");
            this.rconServer.getServer().getLogger().log(Level.INFO, "Rcon connection from [{0}]", ctx.channel().remoteAddress());
        }
        else {
            this.loggedIn = false;
            this.sendResponse(ctx, -1, 2, "");
        }
    }
    
    private void handleCommand(final ChannelHandlerContext ctx, final String payload, final int requestId) {
        if (!this.loggedIn) {
            this.sendResponse(ctx, -1, 2, "");
            return;
        }
        if (this.rconServer.getServer().getPluginManager().dispatchCommand(this.commandSender, payload)) {
            String message = this.commandSender.flush();
            this.sendLargeResponse(ctx, requestId, message);
        }
        else {
            String message = ChatColor.RED + "No such command";
            this.sendLargeResponse(ctx, requestId, String.format("Error executing: %s (%s)", payload, message));
        }
    }
    
    private void sendResponse(final ChannelHandlerContext ctx, final int requestId, final int type, final String payload) {
        final ByteBuf buf = ctx.alloc().buffer().order(ByteOrder.LITTLE_ENDIAN);
        buf.writeInt(requestId);
        buf.writeInt(type);
        buf.writeBytes(payload.getBytes(StandardCharsets.UTF_8));
        buf.writeByte(0);
        buf.writeByte(0);
        ctx.write(buf);
    }
    
    private void sendLargeResponse(final ChannelHandlerContext ctx, final int requestId, final String payload) {
        if (payload.length() == 0) {
            this.sendResponse(ctx, requestId, 0, "");
            return;
        }
        int truncated;
        for (int start = 0; start < payload.length(); start += truncated) {
            final int length = payload.length() - start;
            truncated = (Math.min(length, 2048));
            this.sendResponse(ctx, requestId, 0, payload.substring(start, truncated));
        }
    }
}

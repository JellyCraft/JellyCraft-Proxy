package net.md_5.bungee.rcon.server;

import java.util.List;
import java.nio.ByteOrder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageCodec;

public class RconFramingHandler extends ByteToMessageCodec<ByteBuf>
{
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }
    
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) {
        out.order(ByteOrder.LITTLE_ENDIAN).writeInt(msg.readableBytes());
        out.writeBytes(msg);
    }
    
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        final int length = in.order(ByteOrder.LITTLE_ENDIAN).readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        final ByteBuf buf = ctx.alloc().buffer(length);
        in.readBytes(buf, length);
        out.add(buf);
    }
}

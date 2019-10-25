package netty1.client;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * 
 * 
 * 
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-25
 **/
public class TimeClientHandler extends ChannelHandlerAdapter {

	private static final Logger LOGGER = Logger.getLogger(TimeClientHandler.class.getName());

	private final ByteBuf firstMessage;

	/**
	 * 
	 */
	public TimeClientHandler() {
		byte[] req = "QUERY TIME ORDER".getBytes();
		firstMessage = Unpooled.buffer(req.length);
		firstMessage.writeBytes(req);
	}

	/**
	 * 当客户端和服务器端TCP链路建立成功之后，Netty的NIO线程会调用channelActive方法
	 * 发送查询时间的指令给服务器端，调用ChannelHandlerContext的writeAndFlush方法将请求消息发送给服务器
	 * 
	 * @param ctx
	 *            ChannelHandlerContext
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(firstMessage);
	}

	/**
	 * 当客户端返回应答消息时，channelRead方法会被调用，从ByteBuf中读取并打印应答消息
	 * 
	 * @param ctx
	 *            ChannelHandlerContext
	 * @param msg
	 *            Object
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req, StandardCharsets.UTF_8);
		System.out.println("Now is ：" + body);
	}

	/**
	 * 发生异常时，打印异常日志，释放客户端资源
	 * 
	 * @param ctx
	 *            ChannelHandlerContext
	 * @param cause
	 *            Throwable
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.warning("Unexpected expected from downstream : " + cause.getMessage());
		ctx.close();
	}

}

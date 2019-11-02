package TCP粘包_分隔符和定长解码器.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-25
 **/
public class EchoClientHandler extends ChannelHandlerAdapter {

	private int counter;

	private static final String ECHO_REQ = "Hi~ o(*￣▽￣*)ブ，Mr.Deng，Welcome to Netty.$_";

	/**
	 * Create a client-side handler.
	 */
	EchoClientHandler() {

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
		for (int i = 0; i < 10; i++) {
			ctx.writeAndFlush(Unpooled.copiedBuffer(ECHO_REQ.getBytes()));
		}
	}

	/**
	 * 当客户端返回应答消息时，channelRead方法会被调用
	 * 从ByteBuf中读取并打印应答消息
	 * 
	 * @param ctx
	 *            ChannelHandlerContext
	 * @param msg
	 *            Object
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		System.out.println("The is " + ++counter + " times receive server : [" + msg + "]");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
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
		cause.printStackTrace();
		ctx.close();
	}

}

package tcp_messagepack.TCP粘包_解决.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * 每读到一条消息后，就记一次数，然后发送应答消息给客户端。
 * 按照设计，服务器端接收到消息总数应该跟客户端发送的消息总数相同，而且请求消息删除回车换行符后应该为“QUERY TIME ORDER”
 * 
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-24
 **/
public class TimeServerHandler extends ChannelHandlerAdapter {

	private int counter;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		String body = (String) msg;
		System.out.println("The time server receive order ：" + body + "; the counter is : " + ++counter);
		String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
		currentTime += System.getProperty("line.separator");
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		// 异步发送应答消息给客户端
		ctx.writeAndFlush(resp);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 当异常发生时，关闭ChannelHandlerContext，释放ChannelHandlerContext相关联的句柄等资源
		ctx.close();
	}
}

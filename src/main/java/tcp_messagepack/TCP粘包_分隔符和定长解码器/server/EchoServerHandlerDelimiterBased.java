package tcp_messagepack.TCP粘包_分隔符和定长解码器.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author : Mr.Deng
 * @description : 分隔符解码器
 * @create : 2019-11-02
 **/
public class EchoServerHandlerDelimiterBased extends ChannelHandlerAdapter {

	private int counter = 0;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		// 直接将接受到的消息打印出来
		String body = (String) msg;
		System.out.println("The is " + ++counter + " times receive client : [" + body + "]");
		// 由于我们设置DelimiterBasedFrameDecoder过滤掉了分隔符，所以给客户端时需要在请求消息尾拼接分隔符"$_"
		body += "$_";
		// 最后创建ByteBuf，将原始消息重新返回给客户端
		ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
		// 异步发送应答消息给客户端
		ctx.writeAndFlush(echo);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 发生异常，关闭链路
		ctx.close();
	}
}

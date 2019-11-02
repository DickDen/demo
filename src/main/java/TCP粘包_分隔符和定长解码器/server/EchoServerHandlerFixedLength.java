package TCP粘包_分隔符和定长解码器.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-02
 **/
public class EchoServerHandlerFixedLength extends ChannelHandlerAdapter {

	private int counter = 0;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		// 直接将接受到的消息打印出来
		System.out.println("Receive client : [" + msg + "]");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 发生异常，关闭链路
		ctx.close();
	}
}

package serializable.messagepack.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class EchoServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			// 直接输出msg
			System.out.println("Server receive the messagePack message : " + msg.toString());
			// TODO 回复has receive 给客户端
			ctx.write(msg);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ctx.close();
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 当异常发生时，关闭ChannelHandlerContext，释放ChannelHandlerContext相关联的句柄等资源
		ctx.close();
	}
}

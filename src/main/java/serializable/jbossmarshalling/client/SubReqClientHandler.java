package serializable.jbossmarshalling.client;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import serializable.jbossmarshalling.SubscribeReq;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqClientHandler extends ChannelHandlerAdapter {

	public SubReqClientHandler() {
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
			ctx.write(buildSubReq(i));
			System.out.println(i);
		}
		ctx.flush();
		System.out.println("-----------------send over-----------------");
	}

	private SubscribeReq buildSubReq(int i) {
		SubscribeReq req = new SubscribeReq();
		req.setAddress("BeiJing ChaoYang");
		req.setPhoneNumber("13588888888");
		req.setProductName("Netty Book For Marshalling");
		req.setSubReqId(i);
		req.setUserName("Mr.Deng");
		return req;
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
		System.out.println("Receive server response ；[" + msg + "]");
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
		ctx.close();
		System.out.println("error");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		System.out.println("client:通道可读完成");
	}

}

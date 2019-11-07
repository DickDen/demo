package serializable.googleprotobuf.client;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import serializable.googleprotobuf.SubscribeReqProto;

import java.util.ArrayList;
import java.util.List;

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
		}
		ctx.flush();
		System.out.println("-----------------send over-----------------" + System.currentTimeMillis());
	}

	private SubscribeReqProto.SubscribeReq buildSubReq(int i) {
		SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
		builder.setSubReqId(i);
		builder.setUserName("Mr.Deng");
		builder.setProductName("Netty Book For Protobuf");
		List<String> address = new ArrayList<>();
		address.add("NanJing YuHuaTai");
		address.add("BeiJing LiuLiChang");
		address.add("ShenZhen HongShuLin");
		builder.addAllAddress(address);
		return builder.build();
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
		ctx.flush();
	}

}

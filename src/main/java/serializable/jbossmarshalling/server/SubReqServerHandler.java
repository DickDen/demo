package serializable.jbossmarshalling.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import serializable.jbossmarshalling.SubscribeReq;
import serializable.jbossmarshalling.SubscribeResp;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("active");
		//super.channelActive(ctx);
		System.out.println("active.............");
		ctx.read();
	}

 	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		System.out.println("channelRead");
		System.out.println(msg.toString());
		SubscribeReq req = (SubscribeReq) msg;
		System.out.println("Service accept client subscribe req : [" + msg.toString() + "]");
		ctx.writeAndFlush(buildResp(req.getSubReqId()));
	}

	private SubscribeResp buildResp(int subReqId) {
		SubscribeResp resp = new SubscribeResp();
		resp.setSubReqId(subReqId);
		resp.setRespCode(0);
		resp.setDesc("Netty book order succeed, 3 days later, sent to the designated address");
		return resp;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		System.out.println("channelReadComplete-读完成");
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}
}

package serializable.googleprotobuf.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import serializable.googleprotobuf.SubscribeReqProto;
import serializable.googleprotobuf.SubscribeRespProto;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		SubscribeReqProto.SubscribeReq req = (SubscribeReqProto.SubscribeReq) msg;
		System.out.println("Service accept client subscribe req : [" + req.toString() + "]");
		ctx.writeAndFlush(resp(req.getSubReqId()));
	}

	private SubscribeRespProto.SubscribeResp resp(int subReqId) throws Exception {
		SubscribeRespProto.SubscribeResp.Builder resp = SubscribeRespProto.SubscribeResp.newBuilder();
		resp.setSubReqId(subReqId);
		resp.setRespCode(0);
		resp.setDesc("Netty book order succeed , 3 days later ,sent to the designated address");
		return resp.build();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
}

package TCP粘包_未考虑.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

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
		// 类型转换，将msg转换成Netty的ByteBuf对象,ByteBuf类似于JDK中的java.nio.ByteBuffer对象，不过它提供了更强大和灵活的功能
		ByteBuf buf = (ByteBuf) msg;
		// 通过ByteBuf的readableBytes方法可以获取缓冲区可读的字节数，根据可读的字节数创建bytes数组
		byte[] req = new byte[buf.readableBytes()];
		// 通过ByteBuf的readBytes方法将缓存区的字节数组复制到新建的byte数组中
		buf.readBytes(req);
		// 最后通过new String构造函数获取请求信息
		String body = new String(req, UTF_8).substring(0, req.length - System.getProperty("line.separator").length());
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

package netty1.server;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * TimerServerHandler继承自ChannelHandlerAdapter
 * 它用于对网络时间进行读写操作，通常我们只需要关注channelRead和exceptionCaught方法
 * 
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-24
 **/
public class TimerServerHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		// 类型转换，将msg转换成Netty的ByteBuf对象
		// ByteBuf类似于JDK中的java.nio.ByteBuffer对象，不过它提供了更强大和灵活的功能
		ByteBuf buf = (ByteBuf) msg;
		// 通过ByteBuf的readableBytes方法可以获取缓冲区可读的字节数，根据可读的字节数创建bytes数组
		byte[] req = new byte[buf.readableBytes()];
		// 通过ByteBuf的readBytes方法将缓存区的字节数组复制到新建的byte数组中
		buf.readBytes(req);
		// 最后通过new String构造函数获取请求信息
		String body = new String(req, UTF_8);
		System.out.println("The time server receive order ：" + body);
		String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		// 异步发送应答消息给客户端
		ctx.write(resp);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		// ChannelHandlerContext.flush方法的作用是将消息发送队列中消息写入到SocketChannel中发送给对方
		// 从性能角度考虑，为了防止频繁地唤醒Selector进行消息发送，Netty的write方法并不直接将消息写入到SocketChannel中
		// 调用write方法只是把待发送的消息放到发送缓冲数组中，再通过调用flush方法，将发送缓存区中的消息全部写入到SocketChannel中
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 当异常发生时，关闭ChannelHandlerContext，释放ChannelHandlerContext相关联的句柄等资源
		ctx.close();
	}
}

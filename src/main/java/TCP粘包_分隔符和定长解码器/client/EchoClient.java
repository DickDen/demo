package TCP粘包_分隔符和定长解码器.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-25
 **/
public class EchoClient {

	public static void main(String[] args) {
		int port = 54734;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
				port = 8888;
			}
		}
		new EchoClient().connect(port, "127.0.0.1");
	}

	private void connect(int port, String host) {
		// 配置客户端NIO线程组，创建客户端处理I/O读写的EventLoopGroup线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 创建客户端辅助启动类Bootstrap，对其进行配置
			Bootstrap b = new Bootstrap();
			// 与服务器端不同的是，它的channel需要配置为NioSocketChannel，然后为其添加Handler，此处为了简单直接创建匿名内部类，实现initChannel方法
			// 其作用是当创建NioSocketChannel成功之后，再进行初始化时，将它的ChannelHandle设置到ChannelPipeline中用于处理网络I/O事件
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {

				/**
				 * 首先创建分隔符缓存对象ByteBuf，本例使用“$_”作为分隔符
				 *
				 * @param socketChannel
				 *            socketChannel
				 */
				@Override
				protected void initChannel(SocketChannel socketChannel) {
					ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
					// 第一个参数1024表示单条消息的的最大长度，当达到改长度后仍然没有查找到分隔符，就抛出ToolLongFrameException异常
					// 防止由于异常码流缺失分隔符导致的内存溢出，这是Netty解码器的可靠性保护；第二个参数就是分隔符缓存对象
					// 由于DelimiterBasedFrameDecoder自动对请求消息进行了解码，后续的ChannelHandler接受到的msg对象就是个完整的消息包
					socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
					// 第二个ChannelHandler是StringDecoder，它将ByteBuf解码成字符串对象
					socketChannel.pipeline().addLast(new StringDecoder());
					// 第三个EchoServerHandler接收到的msg消息就是解码后的字符串对象
					socketChannel.pipeline().addLast(new EchoClientHandler());
				}
			});
			// 发起异步连接操作
			// 客户端启动辅助类设置完成之后，调用connect方法发起异步连接，然后调用同步方法等待连接成功
			ChannelFuture future = b.connect(host, port).sync();
			// 等待客户端链接关闭
			// 当客户端关闭之后，客户端主函数退出，退出之前释放NIO线程组的资源
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// 优雅退出，释放NIO线程组
			group.shutdownGracefully();
		}
	}

}

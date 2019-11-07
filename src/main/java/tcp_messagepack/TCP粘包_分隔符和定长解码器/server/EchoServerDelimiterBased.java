package tcp_messagepack.TCP粘包_分隔符和定长解码器.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author : Mr.Deng
 * @description : 分隔符解码器
 * @create : 2019-11-02
 **/
public class EchoServerDelimiterBased {

	public static void main(String[] args) throws InterruptedException {
		int port = 54734;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
				port = 8888;
			}
		}
		new EchoServerDelimiterBased().bind(port);
	}

	private void bind(int port) throws InterruptedException {
		// 配置服务器端的NIO线程组
		// EventLoopGroup是个线程组，它包含了一组NIO线程，专门用于网络事件的处理，实际上它们就是Reactor线程组
		// 一个用于服务器端接受客户端的连接,一个用于进行SocketChannel的网络读写
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			// 创建ServerBootstrap对象，它是Netty用于启动NIO服务器的辅助启动类，目的是降低服务器端的开发复杂度
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					// 设置创建Channel为NioServerSocketChannel，它的功能对应于JDK NIO类库中的ServerSocketChannel类
					.channel(NioServerSocketChannel.class)
					// 设置NioServerSocketChannel的TCP参数
					.option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<SocketChannel>() {

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
							socketChannel.pipeline().addLast(new EchoServerHandlerDelimiterBased());
						}
					});
			// 绑定监听端口，调用它的同步阻塞方法sync等待绑定操作完成
			// 完成之后Netty会返回一个ChannelFuture，它的作用类似于JDK的juc.Future，主要用于异步操作的通知回调
			ChannelFuture f = b.bind(port).sync();
			// 等待服务端监听端口关闭，等待服务端链路关闭之后main函数才退出
			f.channel().closeFuture().sync();
		} finally {
			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}
}

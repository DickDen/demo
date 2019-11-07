package tcp_messagepack.TCP粘包_解决.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author : Mr.Deng
 * @description : Netty时间服务器服务端
 * @create : 2019-10-23
 **/
public class TimeServer {

	public static void main(String[] args) throws InterruptedException {
		int port = 54735;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
				port = 8888;
			}
		}
		new TimeServer().bind(port);
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
					.option(ChannelOption.SO_BACKLOG, 1024)
					// 绑定I/O事件的处理类ChildChannelHandler
					// 它的作用类似于Reactor模式中的Handle类，主要作用于处理网络I/O事件，例如记录日志、对消息进行编解码
					.childHandler(new ChildChannelHandler());
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

	private static class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) {
			// 新增两个解码器：LineBasedFrameDecoder + StringDecoder的组合解释按行切换的文本解码器，它被设计用来支持TCP的粘包和拆包
			/**
			 * LineBasedFrameDecoder的工作原理是它依次遍历ByteBuf中的可读字节，判断看时候有“\n”或者“\r\n”
			 * 如果有，就以此为结束位置，从可读索引到结束位置区间的字节就组成了一行
			 * 它是以换行符为结束标志的解码器，支持携带结束符或者不携带结束符两种解码方式，同时支持配置单行的最大长度
			 * 如果连续读取到最大长度后仍然没有发现换行符，就会抛出异常，同时忽略之前读到的异常码流
			 */
			socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
			/**
			 * StringDecoder的功能是将接受到的对象转换成字符串，然后继续调用后面的Handler
			 */
			socketChannel.pipeline().addLast(new StringDecoder());
			socketChannel.pipeline().addLast(new TimeServerHandler());
		}
	}
}

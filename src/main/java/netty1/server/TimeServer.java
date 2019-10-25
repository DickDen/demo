package netty1.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author : Mr.Deng
 * @description : Netty时间服务器服务端
 * @create : 2019-10-23
 **/
public class TimeServer {

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

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) {
			socketChannel.pipeline().addLast(new TimerServerHandler());
		}
	}
}

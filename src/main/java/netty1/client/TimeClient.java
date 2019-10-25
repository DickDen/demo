package netty1.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author : Mr.Deng
 * @description : Netty时间服务器客户端
 * @create : 2019-10-25
 **/
public class TimeClient {

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
		new TimeClient().connect(port, "127.0.0.1");
	}

	public void connect(int port, String host) {
		// 配置客户端NIO线程组，创建客户端处理I/O读写的EventLoopGroup线程组，
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 创建客户端辅助启动类Bootstrap，对其进行配置
			Bootstrap b = new Bootstrap();
			// 与服务器端不同的是，它的channel需要配置为NioSocketChannel，然后为其添加Handler，此处为了简单直接创建匿名内部类，实现initChannel方法
			// 其作用是当创建NioSocketChannel成功之后，再进行初始化时，将它的ChannelHandle设置到ChannelPipeline中用于处理网络I/O事件
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new TimeClientHandler());
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

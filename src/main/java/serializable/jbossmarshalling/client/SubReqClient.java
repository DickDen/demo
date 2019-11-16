package serializable.jbossmarshalling.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import serializable.jbossmarshalling.MarshallingCodeFactory;

import java.net.InetSocketAddress;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqClient {

	public static void main(String[] args) throws Exception {
		int port = 15474;
		new SubReqClient().bind(port, "127.0.0.1");
	}

	public void bind(int port, String host) throws Exception {
		// 配置客户端NIO线程池
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		try {
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
					.remoteAddress( new InetSocketAddress( host, port ) )
					.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
					ch.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
					ch.pipeline().addLast(new SubReqClientHandler());
				}
			});
			// 发起异步连接操作
			ChannelFuture f = b.connect(host, port).sync();
			// 等待客户端链路关闭
			f.channel().closeFuture().sync();
		} finally {
			// 释放NIO 线程组
			group.shutdownGracefully();
		}

	}
}

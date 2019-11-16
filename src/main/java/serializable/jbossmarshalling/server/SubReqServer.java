package serializable.jbossmarshalling.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import serializable.jbossmarshalling.MarshallingCodeFactory;

import java.net.InetSocketAddress;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqServer {

	public static void main(String[] args) throws Exception {
		int port = 15474;
		new SubReqServer().bind(port);

	}

	public void bind(int port) throws Exception {
		// 配置服务端的NIO线程池
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childOption(ChannelOption.SO_KEEPALIVE,Boolean.TRUE)
					.localAddress( new InetSocketAddress( port ) )
					.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) {
					// 通过MarshallingCodeFactory工厂类创建Marshalling解码器MarshallingDecoder，加入到ChannelPipeline中
					ch.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
					// 通过MarshallingCodeFactory工厂类创建Marshalling解码器MarshallingEncoder，加入到ChannelPipeline中
					ch.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
					ch.pipeline().addLast(new SubReqServerHandler());
				}
			});
			// 绑定端口，等待同步成功
			ChannelFuture f = bootstrap.bind(port).sync();
			// 等待服务端关闭监听端口
			f.channel().closeFuture().sync();
		} finally {
			// 释放线程池资源
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
}


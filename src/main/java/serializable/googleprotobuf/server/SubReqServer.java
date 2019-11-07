package serializable.googleprotobuf.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import serializable.googleprotobuf.SubscribeReqProto;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqServer {

	public static void main(String[] args) throws Exception {
		int port = 15453;
		new SubReqServer().bind(port);

	}

	public void bind(int port) throws Exception {
		// 配置服务端的NIO线程池
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.option(ChannelOption.SO_BACKLOG, 100);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch)  {
					// 向ChannelPipeline添加ProtobufVarint32FrameDecoder，它的主要作用于半包处理，
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					// 添加ProtobufDecoder解码器，它的参数com.google.protobuf.MessageLite，
					// 实际上就是要告诉ProtobufDecoder需要解码的目标类是什么，否则仅仅从字节数组中是无法判断出要解码的目标类型信息
					ch.pipeline().addLast(new ProtobufDecoder(SubscribeReqProto.SubscribeReq.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					// 对消息进行自动解码
					ch.pipeline().addLast(new ProtobufEncoder());
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

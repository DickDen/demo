package serializable.googleprotobuf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import serializable.googleprotobuf.SubscribeRespProto;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class SubReqClient {

	public static void main(String[] args) throws Exception {
		int port = 15453;
		new SubReqClient().bind(port, "127.0.0.1");
	}

	public void bind(int port, String host) throws Exception {
		// 配置客户端NIO线程池
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		try {
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000).handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) {
					// 向ChannelPipeline添加ProtobufVarint32FrameDecoder，它的主要作用于半包处理，
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					// 添加ProtobufDecoder解码器，它的参数com.google.protobuf.MessageLite，
					// 实际上就是要告诉ProtobufDecoder需要解码的目标类是什么，否则仅仅从字节数组中是无法判断出要解码的目标类型信息
					ch.pipeline().addLast(new ProtobufDecoder(SubscribeRespProto.SubscribeResp.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					// 对消息进行自动解码
					ch.pipeline().addLast(new ProtobufEncoder());
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

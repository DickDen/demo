package serializable.messagepack.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import serializable.messagepack.MessagePackDecoder;
import serializable.messagepack.MessagePackEncoder;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class EchoClient {

	public static void main(String[] args) {
		EchoClient client = new EchoClient();
		client.bind(12585, "127.0.0.1");
	}

	private void bind(int port, String host) {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		try {
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
					.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					// 这里设置通过增加包头表示报文长度来避免粘包
					ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
					// 增加解码器
					ch.pipeline().addLast("messagePack decoder", new MessagePackDecoder());
					// 在MessagePack编码器之前增加LengthFieldPrepender，它将在ByteBuf之前增加两个字节的消息长度字段
					ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
					// 增加编码器
					ch.pipeline().addLast("messagePack encoder", new MessagePackEncoder());
					ch.pipeline().addLast(new EchoClientHandler(5));
				}
			});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}

	}
}

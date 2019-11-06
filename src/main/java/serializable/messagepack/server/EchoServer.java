package serializable.messagepack.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import serializable.messagepack.MessagePackDecoder;
import serializable.messagepack.MessagePackEncoder;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class EchoServer {

	public static void main(String[] args) {
		new EchoServer().bind(12585);
	}

	public void bind(int port) {
		EventLoopGroup bossGruop = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		ServerBootstrap bootstrap = new ServerBootstrap();
		try {
			bootstrap.group(bossGruop, workGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.childHandler(new ChannelInitializer<SocketChannel>() {

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
					ch.pipeline().addLast(new EchoServerHandler());
				}
			});
			ChannelFuture future = bootstrap.bind(port).sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGruop.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
}

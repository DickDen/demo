package http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.InetSocketAddress;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-16
 **/
public class HttpFileServer {

	private static final String DEFAULT_URL = "/src/main/java/http/";

	public static void main(String[] args) throws Exception {
		int port = 15474;
		new HttpFileServer().bind(port, DEFAULT_URL);

	}

	public void bind(final int port, final String url) throws Exception {
		// 配置服务端的NIO线程池
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100).childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
					.localAddress(new InetSocketAddress(port)).childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) {
							// HTTP请求消息解码器
							ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
							// HttpObjectAggregator，它的作用是将多个消息转换为单一的FileHttpRequest或者FIleHttpResponse
							// 原因是HTTP解码器再每个HTTP消息中会生成多个消息对象，
							// 1.HttpRequest/HttpResponse 2.HttpContent 3.LastHttpContent
							ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
							// HTTP请求消息编码器
							ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
							// ChunkedWriteHandler它的作用是支持异步发送大的码流，但不占用过多的内存，防止发生Java内存溢出的错误
							ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
							ch.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(url));
						}
					});
			// 绑定端口，等待同步成功
			ChannelFuture f = bootstrap.bind("127.0.0.1", port).sync();
			System.out.println("HTTP 文件服务器启动，网址是 ：" + "127.0.0.1:" + port + url);
			// 等待服务端关闭监听端口
			f.channel().closeFuture().sync();
		} finally {
			// 释放线程池资源
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
}

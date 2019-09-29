package niotest1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-09-05
 **/
public class NIO1 {

	private static final int port = 80;

	public static void main(String[] args) {
		try {
			Object ioHandler = new Object();
			// 1.打开ServerSocketChannel，用于监听客户端的连接，它是所有客户端连接的父管道
			ServerSocketChannel acceptorSvr = ServerSocketChannel.open();
			// 2.绑定监听端口，设置连接为非阻塞模式
			acceptorSvr.socket().bind(new InetSocketAddress(InetAddress.getByName("IP"), port));
			acceptorSvr.configureBlocking(false);
			// 3.创建Reactor线程，创建多路复用器并启动线程
			Selector selector = Selector.open();
			new Thread(new ReactorTask()).start();
			// 4 . ServerSocketChannel注册到Reactor线程的多路复用器Selector上，监听Accept事件
			SelectionKey key = acceptorSvr.register(selector, SelectionKey.OP_READ, ioHandler);
			// 5.多路复用器在线程run方法的无限循环体内轮训准备就绪的key
			int num = selector.select();
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = selectionKeys.iterator();
			while (it.hasNext()) {
				SelectionKey next = it.next();
				// ... deal with I/O event ...
			}
			// 6.多路复用器监听到有新的客户端接入，处理新的接入请求，完成TCP三次握手，建立物理链路
			SocketChannel channel = acceptorSvr.accept();
			// 7.设置客户端链路为非阻塞模式
			channel.configureBlocking(false);
			channel.socket().setReuseAddress(true);
			// 8.将新接入的客户端连接注册到Rector线程的多路复用器上，监听读操作，读取客户端发送的网络信息
			SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_ACCEPT, ioHandler);
			// 9.异步读取客户端请求消息到缓存区
			ByteBuffer receivedBuffer = null;
			channel.read(receivedBuffer);
			// 10. 对ByteBuffer进行编解码，如果有半包消息指针reset，继续读取后续的报文，将编码成功的消息封装成task，投递到业务线程池中，进行业务逻辑编排
			Object message = null;
			List<Object> messageList = new ArrayList<>();
			while (receivedBuffer.hasRemaining()) {
				receivedBuffer.mark();
				Charset charset = Charset.forName("ISO-8859-1");
				CharsetDecoder decode = charset.newDecoder();
				message = decode.decode(receivedBuffer);
				if (message == null) {
					receivedBuffer.reset();
					break;
				}
				messageList.add(message);
			}
			if (!receivedBuffer.hasRemaining()) {
				receivedBuffer.clear();
			} else {
				receivedBuffer.compact();
			}
			if (messageList != null && !messageList.isEmpty()) {
				for (Object messageE : messageList) {
					// handlerTask(messageE);
				}
			}
			// 11.将POJO对象encode成ByteBuffer，调用SocketChannel的异步writer接口，将消息异步发送给客户端
			channel.write(receivedBuffer);
			HashSet set = new HashSet(new ArrayList());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

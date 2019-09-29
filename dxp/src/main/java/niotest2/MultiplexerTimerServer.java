package niotest2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author : Mr.Deng
 * @description : 多路复用类，独立的线程，负责轮询多路复用器Selector，可以处理多个客户端的并发请求
 * @create : 2019-09-19
 **/
public class MultiplexerTimerServer implements Runnable {

	private Selector selector;

	private volatile boolean stop;

	/**
	 * 初始化多路复用器，绑定监听端口
	 * 
	 * @param port
	 *            端口号
	 */
	MultiplexerTimerServer(int port) {
		try {
			// 资源初始化，创建多路复用器Selector，ServerSocketChannel，对Channel和TCP参数进行配置
			selector = Selector.open();
			// ServerSocketChannel 是一个可以监听新进来的TCP连接的通道, 就像标准IO中的ServerSocket一样
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			// 将ServerSocketChannel设置为异步非阻塞模式，它的backlog为1024
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(port), 1024);
			// 初始化成功后，将ServerSocketChannel注册到Selector，监听SelectionKey.OP_ACCEPT操作位
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("The time server is start in port : " + port);
		} catch (IOException e) {
			e.printStackTrace();
			// 资源初始化失败则退出
			System.exit(1);
		}
	}

	public void stop() {
		this.stop = true;
	}

	/**
	 * 线程的run方法循环遍历selectionKeys
	 */
	@Override
	public void run() {
		while (!stop) {
			try {
				// 休眠时间为1秒
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectionKeys.iterator();
				SelectionKey key;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.channel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		// 多路复用器开关，所有注册在上面的Channel和Pipe1等资源都会被自动注册并关闭，所以不需要重复释放资源
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 处理新接入的客户端的请求消息，根据SelectionKey的操作位进行判断即可获知网络事件类型
	 * 
	 * @param key
	 *            selectionKey
	 * @throws IOException
	 *             IOException
	 */
	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			if (key.isAcceptable()) {
				// Accept the new connection
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
				// 通过ServerSocketChannel的accept接受客户端的连接请求并创建SocketChannel实例
				SocketChannel socketChannel = serverSocketChannel.accept();
				// 设置SocketChannel为异步非阻塞
				socketChannel.configureBlocking(false);
				// Add the new connection to the selection
				socketChannel.register(selector, SelectionKey.OP_READ);
			}
			// 读取客户端的请求信息
			if (key.isReadable()) {
				// Read the data
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					// flip的作用是将缓存区当前的limit设置为position,position设置为0,用后续对缓存区的读取操作
					// buffer从写模式切换为读模式
					readBuffer.flip();
					// 根据缓存区可读的字节个数创建字节数组
					byte[] bytes = new byte[readBuffer.remaining()];
					// 将缓存区可读的字节数组复制到新创建的字节数组中
					readBuffer.get(bytes);
					// 创建请求消息体
					String body = new String(bytes, StandardCharsets.UTF_8);
					System.out.println("The time server order ：" + body);
					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
					doWrite(sc, currentTime);
				} else if (readBytes < 0) {
					// 对端链路关闭
					key.channel();
					sc.close();
				} else {
					// 读到0字节，忽略
					System.out.println("读到0字节，忽略");
				}
			}

		}
	}

	/**
	 * 应答消息异步发送给客户端
	 * 
	 * @param channel
	 *            socketChannel
	 * @param response
	 *            响应体内容
	 * @throws IOException
	 *             IOException
	 */
	private void doWrite(SocketChannel channel, String response) throws IOException {
		if (response != null && response.trim().length() > 0) {
			// 将字符串编码成字节数组
			byte[] bytes = response.getBytes();
			// 根据字节数组的容量创建ByteBuffer
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			// 将字节数组复制到缓存区
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
			writeBuffer.get();
		}
	}
}

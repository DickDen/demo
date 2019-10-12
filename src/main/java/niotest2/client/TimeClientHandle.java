package niotest2.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-08
 **/
public class TimeClientHandle implements Runnable {

	private String host;

	private int port;

	private Selector selector;

	private SocketChannel socketChannel;

	private volatile boolean stop;

	/**
	 * 初始化NIO的多路复用器和SocketChannel对象
	 * 
	 * @param host
	 *            IP
	 * @param port
	 *            端口号
	 */
	TimeClientHandle(String host, int port) {
		this.host = host == null ? "127.0.0.0" : host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			// 设置SocketChannel为异步非阻塞模式
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		try {
			// 发送连接请求,作为示例,连接是成功的,所有不需要做重连操作,因此将其放到循环前
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// 轮询多路复用器Selector,当有就绪的Channel时,执行handleInput(key)
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectionKeys.iterator();
				SelectionKey key;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (IOException | InterruptedException e) {
						if (key != null) {
							key.channel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		// 多路复用器关闭后,所有注册在上面的Channel和Pipe等资源都会被自动注册并关闭,所以不需要重复释放资源
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException, InterruptedException {
		if (key.isValid()) {
			// 判断是否连接成功
			SocketChannel sc = (SocketChannel) key.channel();
			// SelectionKey处于连接状态,说明服务器已经返回ACK应答消息
			if (key.isConnectable()) {
				while (!sc.finishConnect()) {
					System.out.println("同" + key + "的连接正在建立，请稍等！");
					Thread.sleep(10);
				}
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				} else {
					// 连接失败,进程结束
					System.exit(1);
				}
			}
			// 客户端接收到了服务器端的应答消息,SelectionKey处于可读状态
			if (key.isReadable()) {
				// 由于我们无法事先判断应答流的大小,我们预留1M的接收缓存区用于读取应答消息
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				// 调用SocketChannel的read()方法进行异步读取操作,由于是异步操作,所以必须对读取的结果进行判断
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, StandardCharsets.UTF_8);
					System.out.println("Now is ：" + body);
					// 当前线程退出循环
					this.stop = true;
				} else if (readBytes < 0) {
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
	 * socket连接
	 * 
	 * @throws IOException
	 *             IOException
	 */
	private void doConnect() throws IOException {
		// 如果连接成功,则注册到多路复用器上,发送请求消息,读应答
		System.out.println("host:" + host + ",port:" + port);
		boolean connect = socketChannel.connect(new InetSocketAddress(host, port));
		if (connect) {
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
			System.out.println("OP_READ");
		} else {
			// 如果没有直接连接成功,说明服务端没有返回TCP握手应答消息,但这并不表示连接失败
			// 我们需要将SocketChannel注册到多路复用器Selector上,注册SelectionKey.OP_CONNECT
			// 当服务器返回TCP syn-ack消息后,Selector就能够轮询到这个SocketChannel处于连接就绪状态
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			System.out.println("OP_CONNECT");
		}
	}

	/**
	 * 构造请求消息体,对其进行编码,写入到发送缓存区中
	 * 
	 * @param sc
	 *            socketChannel
	 * @throws IOException
	 *             IOException
	 */
	private void doWrite(SocketChannel sc) throws IOException {
		byte[] req = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		// 调用SocketChannel的write方法进行发送
		sc.write(writeBuffer);
		// 缓存区的消息全部发送完成
		if (!writeBuffer.hasRemaining()) {
			System.out.println("Send order 2 server succeed.");
		}
	}

}

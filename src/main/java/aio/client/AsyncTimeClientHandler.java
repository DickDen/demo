package aio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-11
 **/
public class AsyncTimeClientHandler implements Runnable, CompletionHandler<Void, AsyncTimeClientHandler> {

	private AsynchronousSocketChannel client;

	private String host;

	private int port;

	private CountDownLatch latch;

	/**
	 * 初始化AsynchronousSocketChannel对象
	 * 
	 * @param host
	 *            IP
	 * @param port
	 *            端口号
	 */
	AsyncTimeClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			// 通过AsynchronousSocketChannel的open方法创建一个新的AsynchronousSocketChannel对象
			client = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void run() {
		// 创建CountDownLatch进行等待，防止异步操作没有执行完成线程就退出
		latch = new CountDownLatch(1);
		// A attachment:AsynchronousSocketChannel的附件，用于回调通知时作为入参被传递，调用者可以自定义
		// CompletionHandler<Void,? super A> handler):异步操作回调通知接口，由调用者实现
		// 本例中这俩参数都用AsyncTimeClientHandler类本身，因为它实现了CompletionHandler接口
		client.connect(new InetSocketAddress(host, port), this, this);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void completed(Void result, AsyncTimeClientHandler attachment) {
		// 创建请求消息体
		byte[] req = "QUERY TIME ORDER".getBytes();
		// 对其进行编码
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		// 复制然后发送缓存区writeBuffer中
		writeBuffer.put(req);
		writeBuffer.flip();
		// 调用AsynchronousSocketChannel的write方法进行异步写
		// 实现CompletionHandler接口用于写操作完成后的回调
		client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer buffer) {
				if (buffer.hasRemaining()) {
					// 发送缓存区中仍有尚未发送的字节，将继续异步发送
					client.write(buffer, buffer, this);
				} else {
					// 已经发送完成，执行异步读取操作
					ByteBuffer readBuffer = ByteBuffer.allocate(1024);
					// 调用AsynchronousSocketChannel的read方法异步读取服务端的响应消息
					// 由于read操作是异步的，所以我们通过内部匿名类实现CompletionHandler<Integer, ByteBuffer>接口，当读取完成被JDK回调时，构造应答消息
					client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {

						@Override
						public void completed(Integer result, ByteBuffer buffer) {
							// 从CompletionHandler中读取应答消息
							buffer.flip();
							byte[] bytes = new byte[buffer.remaining()];
							buffer.get(bytes);
							String body = new String(bytes, StandardCharsets.UTF_8);
							System.out.println("Now is ：" + body);
							latch.countDown();
						}

						@Override
						public void failed(Throwable exc, ByteBuffer attachment) {
							try {
								// 读取发生异常时，关闭链路
								client.close();
							} catch (IOException e) {
								// ignore on close
								System.out.println("ignore on close");
							} finally {
								// 调用CountDownLatch的countDown方法让AsyncTimeClientHandler线程执行完毕，客户端执行退出
								latch.countDown();
							}
						}
					});
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try {
					client.close();
				} catch (IOException e) {
					// ignore on close
					System.out.println("ignore on close");
				} finally {
					latch.countDown();
				}
			}
		});
	}

	@Override
	public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
		exc.printStackTrace();
		try {
			client.close();
		} catch (IOException e) {
			// ignore on close
			System.out.println("ignore on close");
		} finally {
			latch.countDown();
		}
	}
}

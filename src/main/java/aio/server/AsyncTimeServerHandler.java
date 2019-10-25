package aio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-10
 **/
public class AsyncTimeServerHandler implements Runnable {

	private int port;

	CountDownLatch latch;

	AsynchronousServerSocketChannel asynchronousServerSocketChannel;

	AsyncTimeServerHandler(int port) {
		this.port = port;
		try {
			asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
			asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
			System.out.println("The time server is start in port : " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// 在完成一组正在执行的操作之前,允许当前的线程一直阻塞
		latch = new CountDownLatch(1);
		// 接收客户端的连接
		doAccept();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void doAccept() {
		asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
	}

	public int getPort() {
		return port;
	}
}

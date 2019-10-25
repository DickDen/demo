package aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-10
 **/
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

	@Override
	public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
		// 调用AsynchronousServerSocketChannel的accept方法后
		// 如果有新的客户端接入,系统将回调我们传入的AcceptCompletionHandler示例的completed方法,表示新的客户端已经接入成功
		// 因为一个AsynchronousServerSocketChannel可以接收成千上万个客户端,所以需要继续调用它的accept方法,接收其他的客户端连接,最终形成一个循环
		attachment.asynchronousServerSocketChannel.accept(attachment, this);
		// 分配缓存区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		// 进行异步读操作
		// ByteBuffer dst : 接收缓存区,用于从异步Channel中读取数据包
		// A attachment : 异步Channel携带的附件,通知回调的时候作为入参使用
		// CompletionHandler<Integer, ? super A> : 接收通知回调的业务Handler,在本例中为ReadCompletionHandler
		result.read(buffer, buffer, new ReadCompletionHandler(result));
	}

	@Override
	public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
		exc.printStackTrace();
		attachment.latch.countDown();
	}
}

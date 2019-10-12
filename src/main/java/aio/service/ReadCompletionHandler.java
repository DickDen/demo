package aio.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-10
 **/
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

	private AsynchronousSocketChannel channel;

	/**
	 * 将AsynchronousSocketChannel通过参数传递到ReadCompletionHandler中
	 * 当做成员变量来使用，主要用于读取半包消息和发送应答
	 * 
	 * @param channel
	 *            AsynchronousSocketChannel
	 */
	ReadCompletionHandler(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		// 对attachment进行flip，为后续从缓存区读取数据做准备
		// flip的作用是将缓存区当前的limit设置为position,position设置为0,用后续对缓存区的读取操作
		attachment.flip();
		// 根据缓存区的可读字节数创建byte数组
		byte[] body = new byte[attachment.remaining()];
		attachment.get(body);
		// 通过new String方法创建请求消息，对消息进行判断，
		String req = new String(body, StandardCharsets.UTF_8);
		System.out.println("The time server receive order : " + req);
		String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req) ? new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
		// 发送给客户端
		doWrite(currentTime);
	}

	private void doWrite(String currentTime) {
		if (currentTime != null && currentTime.trim().length() > 0) {
			// 将字符串编码成字节数组
			byte[] bytes = currentTime.getBytes();
			// 根据字节数组的容量创建ByteBuffer
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			// 将字节数组复制到缓存区
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {

				@Override
				public void completed(Integer result, ByteBuffer buffer) {
					if (buffer.hasRemaining()) {
						channel.write(buffer, buffer, this);
					}
				}

				@Override
				public void failed(Throwable exc, ByteBuffer attachment) {
					try {
						// 关闭链路，释放资源
						channel.close();
					} catch (IOException e) {
						// ignore on close
					}
				}
			});
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		try {
			// 关闭链路，释放资源
			this.channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

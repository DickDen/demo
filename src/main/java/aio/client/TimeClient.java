package aio.client;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-11
 **/
public class TimeClient {

	public static void main(String[] args) {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
				port = 8888;
			}
		}
		// 实际项目中，我们不需要独立的线程创建异步连接对象，因为底层都是通过JDK的系统回调实现的
		AsyncTimeClientHandler timeClientHandle = new AsyncTimeClientHandler("127.0.0.0", port);

		new Thread(timeClientHandle, "AIO-AsyncTimeClientHandler-001").start();
	}
}

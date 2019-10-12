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
		AsyncTimeClientHandler timeClientHandle = new AsyncTimeClientHandler("127.0.0.0", port);

		new Thread(timeClientHandle, "AIO-AsyncTimeClientHandler-001").start();
	}
}

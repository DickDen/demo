package niotest2.client;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-08
 **/
public class TimeClient {

	public static void main(String[] args) {
		int port = 54734;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
				port = 8888;
			}
		}
		TimeClientHandle timeClientHandle = new TimeClientHandle("127.0.0.0", port);

		new Thread(timeClientHandle, "TimeClient-001").start();
	}
}

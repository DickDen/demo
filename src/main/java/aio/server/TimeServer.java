package aio.server;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-10-10
 **/
public class TimeServer {

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

		AsyncTimeServerHandler timerServer = new AsyncTimeServerHandler(port);

		new Thread(timerServer, "AIO-AsyncTimeServerHandler-001").start();
	}
}

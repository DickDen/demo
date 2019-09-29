package niotest2;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-09-19
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
		MultiplexerTimerServer timerServer = new MultiplexerTimerServer(port);

		new Thread(timerServer, "NIO-MultiplexerTimerServer-001").start();
	}
}

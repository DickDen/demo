package niotest1;

import niotest2.service.MultiplexerTimerServer;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-09-19
 **/
public class TimeServer {

	public static void main(String[] args) {
		int port = 5555;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
				port = 8888;
			}
		}
		MultiplexerTimeServer2 timerServer = new MultiplexerTimeServer2(port);

		new Thread(timerServer, "NIO-MultiplexerTimerServer-001").start();
	}
}

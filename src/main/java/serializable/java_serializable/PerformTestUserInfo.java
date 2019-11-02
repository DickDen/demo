package serializable.java_serializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * @author : Mr.Deng
 * @description : Java序列化编码性能测试类
 * @create : 2019-11-02
 **/
public class PerformTestUserInfo {

	public static void main(String[] args) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.buildUserId(100).buildUserName("Welcome to Netty");
		int loop = 1000000;
		ByteArrayOutputStream bos;
		ObjectOutputStream os;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			bos = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bos);
			os.writeObject(userInfo);
			os.flush();
			os.close();
			byte[] b = bos.toByteArray();
			bos.close();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("THe JDK serializable cost time is : " + (endTime - startTime) + "ms");

		System.out.println("---------------------------------------------");

		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		startTime = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			userInfo.codec(byteBuffer);
		}
		endTime = System.currentTimeMillis();
		System.out.println("THe byte array serializable cost time is : " + (endTime - startTime) + "ms");
	}
}

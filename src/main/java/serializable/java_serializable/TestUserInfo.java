package serializable.java_serializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author : Mr.Deng
 * @description : Java序列化编码测试类
 * @create : 2019-11-02
 **/
public class TestUserInfo {

	public static void main(String[] args) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.buildUserId(100).buildUserName("Welcome to Netty");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bos);
		os.writeObject(userInfo);
		os.flush();
		os.close();
		byte[] b = bos.toByteArray();
		System.out.println("The JDK serializable length is : " + b.length);
		bos.close();
		System.out.println("------------------------------------------");
		System.out.println("The byte array serializable length is : " + userInfo.codec().length);
	}
}

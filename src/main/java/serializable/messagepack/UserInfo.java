package serializable.messagepack;

import org.msgpack.annotation.Message;

/**
 * @author : Mr.Deng
 * @description : 用户信息
 * @create : 2019-11-06
 **/
@Message
public class UserInfo {

	private String userName;

	private int age;

	public UserInfo() {
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "UserInfo{" + "userName='" + userName + '\'' + ", age=" + age + '}';
	}
}
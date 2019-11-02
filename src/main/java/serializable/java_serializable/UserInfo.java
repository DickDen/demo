package serializable.java_serializable;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author : Mr.Deng
 * @description : 用户信息
 * @create : 2019-11-02
 **/
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 7203225093658302101L;

	private String userName;

	private int userId;

	public void buildUserName(String userName) {
		this.userName = userName;
	}

	public UserInfo buildUserId(int userId) {
		this.userId = userId;
		return this;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * 使用基于ByteBuffer的通用二进制编码技术对UserInfo对象进行编码，编码结果是byte数组，可以与传统的JDK序列化后的码流大小进行对比
	 * 
	 * @return byte[]
	 */
	byte[] codec() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		return getBytes(buffer);
	}

	public byte[] codec(ByteBuffer buffer) {
		buffer.clear();
		return getBytes(buffer);
	}

	private byte[] getBytes(ByteBuffer buffer) {
		byte[] value = this.userName.getBytes();
		buffer.putInt(value.length);
		buffer.put(value);
		buffer.putInt(userId);
		buffer.flip();
		value = null;
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);
		return result;
	}
}
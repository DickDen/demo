package serializable.googleprotobuf;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Mr.Deng
 * @description :
 * @create : 2019-11-06
 **/
public class TestSubscribeReqProto {

	/**
	 * 编码
	 * 
	 * @param req
	 *            SubscribeReqProto.SubscribeReq
	 * @return byte[]
	 */
	private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
		return req.toByteArray();
	}

	/**
	 * 解码
	 * 
	 * @param body
	 *            byte[]
	 * @return SubscribeReqProto.SubscribeReq
	 * @throws InvalidProtocolBufferException
	 *             异常
	 */
	private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
		return SubscribeReqProto.SubscribeReq.parseFrom(body);
	}

	private static SubscribeReqProto.SubscribeReq createSubscribeReq() {
		SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
		builder.setSubReqId(1);
		builder.setUserName("Mr.Deng");
		builder.setProductName("Netty Book");
		List<String> address = new ArrayList<>();
		address.add("BeiJing");
		address.add("ShanXi");
		address.add("TianJin");
		builder.addAllAddress(address);
		return builder.build();
	}

	public static void main(String[] args) throws InvalidProtocolBufferException {
		SubscribeReqProto.SubscribeReq req = createSubscribeReq();
		System.out.println("Before encode : " + req.toString());
		SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
		System.out.println("After encode : " + req.toString());
		System.out.println("Assert equal : --> " + req2.equals(req));

	}
}

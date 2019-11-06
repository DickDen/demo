package serializable.messagepack;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author : Mr.Deng
 * @description : 编码器
 * @create : 2019-11-04
 **/
public class MessagePackEncoder extends MessageToByteEncoder<Object> {

	/**
	 * MessageToByteEncoder 负责将Object类型的POJO对象编码为byte数组，然后写入到ByteBuf中
	 * 
	 * @param channelHandlerContext
	 *            channelHandlerContext
	 * @param msg
	 *            Object
	 * @param byteBuf
	 *            ByteBuf
	 * @throws Exception
	 *             exception
	 */
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
		MessagePack messagePack = new MessagePack();
		// 编码，然后转为ByteBuf传递
		byteBuf.writeBytes(messagePack.write(msg));
	}
}

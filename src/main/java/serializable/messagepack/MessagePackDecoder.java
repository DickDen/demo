package serializable.messagepack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * @author : Mr.Deng
 * @description : 解码器
 * @create : 2019-11-04
 **/
public class MessagePackDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		// 获取需要解码数据的长度
		final int length = byteBuf.readableBytes();
		// 新创建一个字节数组，其长度设置为上面获取的长度
		byte[] b = new byte[length];
		// 将要解码的数据填充到新创建的数组中
		byteBuf.getBytes(byteBuf.readerIndex(), b,0,length);
		MessagePack msgpack = new MessagePack();
		// 调用MessagePack的read方法将其反序列化为Object对象，将解码后的对象加入到解码列表List<Object>中
		list.add(msgpack.read(b));
	}
}

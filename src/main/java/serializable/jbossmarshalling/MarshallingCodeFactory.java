package serializable.jbossmarshalling;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

import io.netty.handler.codec.marshalling.*;

/**
 * @author : Mr.Deng
 * @description : Marshalling工厂类
 * @create : 2019-11-07
 **/
public class MarshallingCodeFactory {

	/**
	 * 创建JBoss Marshalling解码器MarshallingDecoder
	 * 
	 * @return MarshallingDecoder
	 */
	public static MarshallingDecoder buildMarshallingDecoder() {
		// 通过Marshalling工具类的getProvidedMarshallerFactory的静态方法获取MarshallerFactory实例，参数【serial】表示创建的是java序列化工厂对象
		final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
		// 创建MarshallingConfiguration对象，版本号设置为5
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		// 根据MarshallerFactory和MarshallingConfiguration创建UnmarshallerProvider
		UnmarshallerProvider provider = new DefaultUnmarshallerProvider(marshallerFactory, configuration);
		// 通过构造函数创建Netty的MarshallingDecoder对象，它有两个参数，分别是UnmarshallerProvider和单个消息序列化之后的最大长度
		return new MarshallingDecoder(provider, 1024);
	}

	/**
	 * 建JBoss Marshalling编码器MarshallingEncoder
	 * 
	 * @return MarshallingEncoder
	 */
	public static MarshallingEncoder buildMarshallingEncoder() {
		final MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		MarshallerProvider provider = new DefaultMarshallerProvider(factory, configuration);
		// MarshallingEncoder用于将实现序列化接口的pojo对象序列化为二进制数组
		return new MarshallingEncoder(provider);
	}

}

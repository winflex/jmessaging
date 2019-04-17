package messaging.common.serialize;

import java.io.IOException;

/**
 * 序列化/反序列化接口定义
 * 
 * @author winflex
 */
public interface ISerializer {
	
	byte[] serialize(Object obj) throws IOException;
	
	<T> T deserialize(byte[] serializedData) throws IOException, ClassNotFoundException;
}

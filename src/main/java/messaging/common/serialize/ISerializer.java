package messaging.common.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化/反序列化接口定义
 * 
 * @author winflex
 */
public interface ISerializer {
	
	void serialize(Object obj, OutputStream out) throws IOException;
	
	<T> T deserialize(InputStream in) throws IOException;
}

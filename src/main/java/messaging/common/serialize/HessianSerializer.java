package messaging.common.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;

/**
 * 
 * @author winflex
 */
public class HessianSerializer implements ISerializer {

	@Override
	public byte[] serialize(Object obj) throws IOException {
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(baos);
        ho.setSerializerFactory(SERIALIZER_FACTORY);
        ho.writeObject(obj);
        ho.flush();
        return baos.toByteArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(byte[] stream) throws IOException, ClassNotFoundException {
		 Hessian2Input hi = new Hessian2Input(new ByteArrayInputStream(stream));
        hi.setSerializerFactory(SERIALIZER_FACTORY);
        return (T) hi.readObject();
	}
	
	private final SerializerFactory SERIALIZER_FACTORY = new SerializerFactory() {
		@Override
	    public ClassLoader getClassLoader() {
	        return Thread.currentThread().getContextClassLoader();
	    }
	};
}

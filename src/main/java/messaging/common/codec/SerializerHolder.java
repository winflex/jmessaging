package messaging.common.codec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import messaging.common.serialize.ISerializer;
import messaging.util.ExtensionLoader;

/**
 * 
 * @author winflex
 */
class SerializerHolder {
	
	static ISerializer getSerializer(int code) throws Exception {
		return serializers.get(String.valueOf(code));
	}
	
	private static final Map<String, ISerializer> serializers = new HashMap<>();
	static {
		try {
			ExtensionLoader loader = ExtensionLoader.getLoader(ISerializer.class);
			Set<String> exts = loader.listExtensions();
			for (String ext : exts) {
				serializers.put(ext, loader.getExtension(ext));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}
}

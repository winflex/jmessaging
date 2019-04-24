package messaging.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 静态扩展点机制
 * 
 * @author winflex
 */
public final class ExtensionLoader {
	private static final String EXTENSION_DIRECTORY = "META-INF/extension/";
	private static final ConcurrentMap<Class<?>, ExtensionLoader> EXTENSION_LOADER_CACHE = new ConcurrentHashMap<Class<?>, ExtensionLoader>();

	private final Class<?> type;
	private volatile Map<String, String> extensionClassCache;

	private ExtensionLoader(Class<?> type) {
		this.type = type;
	}

	public static ExtensionLoader getLoader(Class<?> type) {
		if (type == null)
			throw new IllegalArgumentException("type is null");
		if (!type.isInterface()) {
			throw new IllegalArgumentException(type.getName() + " is not an interface!");
		}

		ExtensionLoader loader = EXTENSION_LOADER_CACHE.get(type);
		if (loader == null) {
			EXTENSION_LOADER_CACHE.putIfAbsent(type, new ExtensionLoader(type));
			loader = (ExtensionLoader) EXTENSION_LOADER_CACHE.get(type);
		}
		return loader;
	}

	@SuppressWarnings("unchecked")
	public <T> T getExtension(String name) throws Exception {
		Map<String, String> classCache = extensionClassCache;
		if (classCache == null) {
			synchronized (this) {
				if ((classCache = extensionClassCache) == null) {
					classCache = extensionClassCache = findExtensionClasses();
				}
			}
		}
		String className = classCache.get(name);
		if (className == null) {
			throw new Exception("No such extension " + name + " of type " + type.getName());
		}
		return (T) Class.forName(className).newInstance();
	}
	
	public Set<String> listExtensions() throws Exception {
		Map<String, String> classCache = extensionClassCache;
		if (classCache == null) {
			synchronized (this) {
				if ((classCache = extensionClassCache) == null) {
					classCache = extensionClassCache = findExtensionClasses();
				}
			}
		}
		return classCache.keySet();
	}

	private Map<String, String> findExtensionClasses() throws Exception {
		Map<String, String> cache = new HashMap<String, String>();

		Enumeration<URL> extensionFiles = getExtensionFiles();
		if (extensionFiles != null) {
			while (extensionFiles.hasMoreElements()) {
				pasreExtensionFile(extensionFiles.nextElement(), cache);
			}
		}
		return cache;
	}

	private Enumeration<URL> getExtensionFiles() throws IOException {
		String fileName = EXTENSION_DIRECTORY + type.getName();
		ClassLoader classLoader = getClass().getClassLoader();
		if (classLoader != null) {
			return classLoader.getResources(fileName);
		} else {
			return ClassLoader.getSystemResources(fileName);
		}
	}

	private void pasreExtensionFile(URL file, Map<String, String> extensionClassCache) throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.openStream()))) {
			Properties properties = new Properties();
			properties.load(reader);
			for (String extensionName : properties.stringPropertyNames()) {
				String extensionClass = properties.getProperty(extensionName);
				if (extensionClass == null || extensionClass.trim().isEmpty()) {
					continue;
				}
				extensionClassCache.put(extensionName, extensionClass.trim());
			}
		}
	}
}

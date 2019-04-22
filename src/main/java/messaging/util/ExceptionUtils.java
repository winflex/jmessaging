package messaging.util;

import java.util.function.Supplier;

/**
 * 
 * @author winflex
 */
public class ExceptionUtils {
	@SuppressWarnings("unchecked")
	public static <T> T as(Throwable t, Class<T> clazz, Supplier<T> supplier) {
    	if (clazz.isAssignableFrom(t.getClass())) {
    		return (T) t;
    	} else {
    		return supplier.get();
    	}
    }
}

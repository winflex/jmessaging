
package messaging.util;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * 
 * 
 * @author winflex
 */
public final class TimeUtils {

    public static long currentTime() {
        return System.nanoTime();
    }

    public static long elapsedMillis(final long startNanoTime) {
        return NANOSECONDS.toMillis(System.nanoTime() - startNanoTime);
    }

    public static long elapsedMillis(final long startNanoTime, final long endNanoTime) {
        return NANOSECONDS.toMillis(endNanoTime - startNanoTime);
    }
}
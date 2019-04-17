
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

    public static long elapsedMillis(final long startTime) {
        return NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    public static long elapsedMillis(final long startTime, final long endTime) {
        return NANOSECONDS.toMillis(endTime - startTime);
    }
}
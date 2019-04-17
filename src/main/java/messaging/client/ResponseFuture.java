package messaging.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messaging.util.concurrent.DefaultPromise;
import messaging.util.concurrent.NamedThreadFactory;

/**
 * 内置超时监控的Future
 * 
 * @author winflex
 */
public class ResponseFuture<T> extends DefaultPromise<T> {

	private static final long serialVersionUID = 275517284412500195L;

	private static final Logger logger = LoggerFactory.getLogger(ResponseFuture.class);

	private static final ConcurrentMap<Long, ResponseFuture<?>> inflightFutures = new ConcurrentHashMap<>();

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("ResponseFuture-Watchdog", true));

	public static void doneWithResult(long futureId, Object result) {
		ResponseFuture<?> future = inflightFutures.get(futureId);
		if (future == null) {
			logger.warn("No future correlate with " + futureId + ", maybe it's timed out");
			return;
		}
		future.cancelTimeoutTask();
		future.setSuccess(result);
	}

	public static void doneWithException(long futureId, Throwable cause) {
		ResponseFuture<?> future = inflightFutures.get(futureId);
		if (future == null) {
			logger.warn("No future correlate with " + futureId + ", maybe it's timed out");
			return;
		}
		future.cancelTimeoutTask();
		future.setFailure(cause);
	}

	private final long futureId;

	private final ScheduledFuture<?> timeoutFuture;

	public ResponseFuture(int timeoutMillis) {
		if (timeoutMillis <= 0) {
			throw new IllegalArgumentException("timeoutMillis must be positive");
		}
		
		this.futureId = sequence.incrementAndGet();
		inflightFutures.put(futureId, this);
		this.timeoutFuture = scheduler.schedule(() -> {
			setFailure(new TimeoutException("timed out after " + timeoutMillis + "ms"));
		}, timeoutMillis, TimeUnit.MILLISECONDS);
	}

	private void cancelTimeoutTask() {
		timeoutFuture.cancel(true);
	}

	public final long getRequestId() {
		return futureId;
	}
	
	private static final AtomicLong sequence = new AtomicLong();
}

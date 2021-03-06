package messaging.client;

import static messaging.util.TimeUtils.currentTime;
import static messaging.util.TimeUtils.elapsedMillis;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import messaging.util.ExceptionUtils;
import messaging.util.concurrent.NamedThreadFactory;

/**
 * 连接池
 * 
 * 
 * @author winflex
 */
@Slf4j
public class ChannelGroup {

	private static final ScheduledExecutorService reconnectExecutor = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory("Reconnect-Thread", true));

	private final Bootstrap bootstrap;
	private final HealthChecker healthChecker;

	private final AtomicReferenceArray<Channel> channels;
	private final AtomicInteger selectIndex = new AtomicInteger();
	
	private final ReconnectTask reconnectTask;

	private final AtomicBoolean closed = new AtomicBoolean();

	public ChannelGroup(Bootstrap bootstrap, int maxConnections, HealthChecker healthChecker) throws IOException {
		if (maxConnections <= 0) {
			throw new IllegalArgumentException("maxConnections must be positive");
		}
		this.bootstrap = bootstrap;
		this.healthChecker = healthChecker;
		this.channels = new AtomicReferenceArray<>(maxConnections);
		for (int i = 0; i < maxConnections; i++) {
			channels.set(i, connect());
		}
		this.reconnectTask = new ReconnectTask();
	}

	private Channel connect() throws IOException {
		ChannelFuture future = bootstrap.connect().syncUninterruptibly();
		if (future.isSuccess()) {
			return future.channel();
		} else {
			throw ExceptionUtils.as(future.cause(), IOException.class, () -> new IOException(future.cause()));
		}
	}

	public Channel getChannel(int timeoutMillis) throws TimeoutException {
		final int total = channels.length();
		final long start = currentTime();
		for (int i = 0; elapsedMillis(start) < timeoutMillis; i++) {
			Channel ch = channels.get(this.selectIndex.getAndIncrement() % total);
			if (healthChecker.isHealthy(ch)) { // check on checkout
				return ch;
			}
			if (closed.get()) {
				throw new IllegalStateException("Already closed");
			}
			if ((i & 0xff) == 0xff) {
				LockSupport.parkNanos(10);
			}
		}

		throw new TimeoutException("get channel timed out after " + elapsedMillis(start));
	}

	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}
		reconnectTask.stop();
		for (int i = 0; i < channels.length(); i++) {
			channels.get(i).close();
		}
	}

	public final boolean isClosed() {
		return this.closed.get();
	}

	public static interface HealthChecker {

		public static final HealthChecker ACTIVE = (channel) -> channel.isActive(); 
		public static final HealthChecker ALWAYS_HEALTHY = (channel) -> true;
		public static final HealthChecker NEVER_HEALTHY = (channel) -> false;

		boolean isHealthy(Channel channel);
	}

	/**
	 * 重连任务
	 */
	private final class ReconnectTask implements Runnable {

		private final ConcurrentMap<Integer, ChannelFuture> inflightFutures = new ConcurrentHashMap<>();
		private final ScheduledFuture<?> scheduledFuture;
		
		public ReconnectTask() {
			scheduledFuture = reconnectExecutor.scheduleWithFixedDelay(this, 500, 50, TimeUnit.MILLISECONDS);
		}
		
		void stop() {
			scheduledFuture.cancel(true);
		}
		
		@Override
		public void run() {
			for (int i = 0; i < channels.length(); i++) {
				final int index = i;
				ChannelFuture oldFuture = inflightFutures.get(index);
				if (oldFuture != null && !oldFuture.isDone()) { // already reconnecting
					continue;
				}
				final Channel ch = channels.get(i);
				if (!healthChecker.isHealthy(ch)) {
					ChannelFuture future = bootstrap.connect();
					inflightFutures.put(index, future);
					future.addListener(new ChannelFutureListener() {

						@Override
						public void operationComplete(ChannelFuture f) throws Exception {
							if (f.isSuccess()) {
								channels.set(index, f.channel());
							} else {
								log.error("Reconnect failed", f.cause());
							}
						}
					});
				}
			}
		}

	}
}

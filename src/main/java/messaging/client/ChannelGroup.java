package messaging.client;

import static messaging.util.TimeUtils.*;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import messaging.util.Endpoint;

/**
 * 连接池
 * 
 * 
 * @author winflex
 */
public class ChannelGroup {

	private final Deque<Channel> channels = new ConcurrentLinkedDeque<>();

	private final int maxConnections;
	private final Endpoint endpoint;
	private final Bootstrap bootstrap;

	private final HealthChecker healthChecker;
	private final AtomicBoolean closed = new AtomicBoolean();

	// 正在创建的连接的数量
	private final AtomicInteger connectingCount = new AtomicInteger();

	public ChannelGroup(Endpoint endpoint, Bootstrap bootstrap, int maxConnections, HealthChecker healthChecker)
			throws IOException {
		if (maxConnections <= 0) {
			throw new IllegalArgumentException("maxConnections must be positive");
		}
		this.maxConnections = maxConnections;
		this.endpoint = endpoint;
		this.bootstrap = bootstrap;
		this.healthChecker = healthChecker;

		for (int i = 0; i < maxConnections; i++) {
			channels.add(syncConnect());
		}
	}

	private Channel syncConnect() throws IOException {
		ChannelFuture future = asyncConnect().syncUninterruptibly();
		if (future.isSuccess()) {
			return future.channel();
		} else {
			Throwable cause = future.cause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			} else {
				throw new IOException(cause);
			}
		}
	}

	private ChannelFuture asyncConnect() {
		return bootstrap.connect(endpoint.getIp(), endpoint.getPort());
	}

	public Channel getChannel(int timeoutMillis) throws TimeoutException {
		if (closed.get()) {
			throw new IllegalStateException("Already closed");
		}

		final long startTime = currentTime();
		do {
			Channel channel = channels.poll();
			if (channel == null) {
				fill(maxConnections);
				LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(1));
			} else if (!healthChecker.isHealthy(channel)) {
				channel.close();
				fill(1);
			} else {
				channels.offer(channel);
				return channel;
			}
		} while (elapsedMillis(startTime) < timeoutMillis);
		
		throw new TimeoutException("get channel timed out after " + elapsedMillis(startTime) + "ms");
	}

	private synchronized void fill(final int need) {
		int actualNeed = need - connectingCount.get(); // 减去已经在创建的数量
		for (int i = 0; i < actualNeed; i++) {
			connectingCount.incrementAndGet();
			asyncConnect().addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					connectingCount.decrementAndGet();
					if (future.isSuccess()) {
						channels.offer(future.channel());
					}
				}
			});
		}
	}

	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}

		Channel ch;
		while ((ch = channels.poll()) != null) {
			ch.close();
		}
	}

	public final boolean isClosed() {
		return this.closed.get();
	}

	public static interface HealthChecker {

		public static final HealthChecker ACTIVE = new HealthChecker() {

			@Override
			public boolean isHealthy(Channel channel) {
				return channel.isActive();
			}
		};

		boolean isHealthy(Channel channel);
	}
}

package messaging.client;

import static messaging.util.TimeUtils.currentTime;
import static messaging.util.TimeUtils.elapsedMillis;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import messaging.util.concurrent.NamedThreadFactory;

/**
 * 连接池
 * 
 * 
 * @author winflex
 */
public class ChannelGroup {
	
	private static final Logger logger = LoggerFactory.getLogger(ChannelGroup.class);
	
	private static final Executor reconnectExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Reconnect-Thread"));
	
	private final Bootstrap bootstrap;
	private final HealthChecker healthChecker;

	private final AtomicReferenceArray<Channel> channels;
	private final AtomicInteger index = new AtomicInteger();

	private final AtomicBoolean closed = new AtomicBoolean();

	public ChannelGroup(Bootstrap bootstrap, int maxConnections, HealthChecker healthChecker) throws IOException {
		if (maxConnections <= 0) {
			throw new IllegalArgumentException("maxConnections must be positive");
		}
		this.bootstrap = bootstrap;
		this.healthChecker = healthChecker;
		this.channels = new AtomicReferenceArray<>(maxConnections);
		for (int i = 0; i < maxConnections; i++) {
			connectAndSetChannel(i);
		}
	}

	private void connectAndSetChannel(int index) throws IOException {
		Channel ch = connect();
		ch.closeFuture().addListener((f) -> {
			reconnectExecutor.execute(() -> {
				try {
					connectAndSetChannel(index);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			});
		});
		channels.set(index, ch);
	}

	private Channel connect() throws IOException {
		ChannelFuture future = bootstrap.connect().syncUninterruptibly();
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

	public Channel getChannel(int timeoutMillis) throws TimeoutException {
		final int total = channels.length();
		final long start = currentTime();
		for (int i = 0; elapsedMillis(start) < timeoutMillis; i++) {
			Channel ch = channels.get(this.index.getAndIncrement() % total);
			if (healthChecker.isHealthy(ch)) {
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

		for (int i = 0; i < channels.length(); i++) {
			channels.get(i).close();
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

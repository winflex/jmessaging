package messaging.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.IdleStateHandler;
import messaging.common.RpcException;
import messaging.common.codec.Decoder;
import messaging.common.codec.Encoder;
import messaging.util.NettyUtils;
import messaging.util.concurrent.DefaultPromise;
import messaging.util.concurrent.IFuture;
import messaging.util.concurrent.NamedThreadFactory;

/**
 * 
 * @author winflex
 */
public class RpcServer {
	private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

	private final RpcServerOptions options;

	private Executor executor;

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel serverChannel;

	private final Map<String, IRequestHandler<?>> handlers = new HashMap<>();

	private final AtomicBoolean closed = new AtomicBoolean();
	private final DefaultPromise<Void> closeFuture = new DefaultPromise<>();

	public RpcServer(int port) {
		this(new RpcServerOptions(port));
	}

	public RpcServer(RpcServerOptions options) {
		this.options = options;
	}

	public RpcServer start() throws RpcException {
		ServerBootstrap boostrap = createBootstrap();
		ChannelFuture f = null;
		f = boostrap.bind(options.getBindIp(), options.getPort()).syncUninterruptibly();

		if (f.isSuccess()) {
			this.serverChannel = f.channel();
			logger.info("Server is now listening on {}:{}", options.getBindIp(), options.getPort());
		} else {
			throw new RpcException(f.cause());
		}
		return this;
	}

	private ServerBootstrap createBootstrap() {
		ServerBootstrap boostrap = new ServerBootstrap();
		if (Epoll.isAvailable()) {
			this.bossGroup = new EpollEventLoopGroup(1, new NamedThreadFactory("RpcServer-IoAcceptor"));
			this.workerGroup = new EpollEventLoopGroup(options.getIoThreads(), new NamedThreadFactory("RpcServer-IoWorker"));
			boostrap.channel(EpollServerSocketChannel.class);
			boostrap.option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			NettyUtils.fillTcpOptions(boostrap, options, true);
			boostrap.childOption(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		} else {
			this.bossGroup = new NioEventLoopGroup(1, new NamedThreadFactory("RpcServer-IoAcceptor"));
			this.workerGroup = new NioEventLoopGroup(options.getIoThreads(), new NamedThreadFactory("RpcServer-IoWorker"));
			boostrap.channel(NioServerSocketChannel.class);
			boostrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			NettyUtils.fillTcpOptions(boostrap, options, false);
			boostrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}
		boostrap.group(bossGroup, workerGroup);
		boostrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				logger.info("Channel connected, channel = {}", ch);
				ch.closeFuture().addListener((future) -> {
					logger.info("Channel disconnected, channel = {}", ch);
				});
				ChannelPipeline pl = ch.pipeline();
				
				// https://github.com/relayrides/pushy/pull/657
				// https://github.com/netty/netty/issues/1759
				pl.addLast(new FlushConsolidationHandler(256, true));
				pl.addLast(new IdleStateHandler(options.getIdleTimeout(), 0, 0, TimeUnit.MILLISECONDS));
				pl.addLast(new Decoder());
				pl.addLast(new Encoder());
				pl.addLast(new ServerHandler(RpcServer.this));
			}
		});
		return boostrap;
	}

	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}

		if (serverChannel != null) {
			serverChannel.close().syncUninterruptibly();
		}
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
		logger.info("Server shutdown");
		closeFuture.setSuccess(null);
	}

	public void registerHandler(IRequestHandler<?> handler) {
		final String className = handler.interestClass();
		if (handlers.containsKey(className)) {
			throw new RuntimeException("handler of " + className + " already registered");
		}
		handlers.put(className, handler);
	}

	@SuppressWarnings("unchecked")
	public <T> IRequestHandler<T> getHandler(String name) {
		return (IRequestHandler<T>) handlers.get(name);
	}

	public final void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public final Executor getExecutor() {
		return executor;
	}	

	public final RpcServerOptions getOptions() {
		return options;
	}

	public final IFuture<Void> closeFuture() {
		return this.closeFuture;
	}
}

package messaging.client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import messaging.client.ChannelGroup.HealthChecker;
import messaging.common.RpcException;
import messaging.common.codec.Decoder;
import messaging.common.codec.Encoder;
import messaging.common.protocol.RpcRequest;
import messaging.util.Endpoint;
import messaging.util.NettyUtils;
import messaging.util.concurrent.IFuture;
import messaging.util.concurrent.NamedThreadFactory;

/**
 * RPC客户端
 * 
 * 
 * @author winflex
 */
public class RpcClient {
	private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

	private final RpcClientOptions options; // 配置
	private final EventLoopGroup workerGroup;
	private final ChannelGroup channelGroup;

	private final AtomicBoolean closed = new AtomicBoolean();

	public RpcClient(Endpoint endpoint) throws IOException {
		this(new RpcClientOptions(endpoint));
	}

	public RpcClient(RpcClientOptions options) throws IOException {
		this.options = options;
		this.workerGroup = new NioEventLoopGroup(options.getIoThreads(), new NamedThreadFactory("Rpc-Client-IoWorker"));
		this.channelGroup = new ChannelGroup(createBootstrap(), options.getMaxConnections(), HealthChecker.ACTIVE);
	}

	private Bootstrap createBootstrap() {
		Bootstrap b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		final Endpoint endpoint = options.getEndpoint();
		b.remoteAddress(endpoint.getIp(), endpoint.getPort());
		b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.getConnectTimeoutMillis());
		NettyUtils.fillTcpOptions(b, options.getTcpOptions());
		b.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				logger.info("Channel connected, channel = {}", ch);
				ch.closeFuture().addListener((future) -> {
					logger.info("Channel disconnected, channel = {}", ch);
				});

				final ChannelPipeline pl = ch.pipeline();
				pl.addLast(new Decoder());
				pl.addLast(new Encoder());
				pl.addLast(new ResponseHandler());
			}
		});
		return b;
	}
	
	

	/**
	 * 同步发送无响应的请求
	 * 
	 * @throws RpcException 当发送失败时
	 */
	public void sendSync(Object data, int timeoutMillis) throws RpcException {
		IFuture<Void> future = sendAsync(data, timeoutMillis).awaitUninterruptibly();
		if (!future.isSuccess()) {
			if (future.cause() instanceof RpcException) {
				throw (RpcException) future.cause();
			} else {
				throw new RpcException(future.cause());
			}
		}
	}
	
	/**
	 * 异步发送无响应的请求
	 * 
	 * @return future, 当发送成功时, 该future成功完成, 否则, 该future失败完成, 无论如何, 该future都确保在timeoutMillis时间内完成
	 */
	public IFuture<Void> sendAsync(Object data, int timeoutMillis) {
		final RpcRequest request = new RpcRequest(data, true);
		final ResponseFuture<Void> future = new ResponseFuture<>(request.getId(), timeoutMillis);
		try {
			Channel ch = channelGroup.getChannel(options.getConnectTimeoutMillis());
			request.setSerializerCode(options.getSerializerCode());
			NettyUtils.writeAndFlush(ch, request).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (channelFuture.isSuccess()) {
						future.setSuccess(true);
					} else {
						future.setFailure(channelFuture.cause());
					}
				}
			});
		} catch (TimeoutException e) {
			future.setFailure(e);
		}
		return future;
	}
	
	/**
	 * 同步发送需要响应的请求
	 * 
	 * @return 响应
	 * @throws RpcException 当请求失败时抛出
	 */ 
	public <V> V requestSync(Object data, int timeoutMillis) throws RpcException {
		IFuture<V> future = requestAsync(data, timeoutMillis);
		future.awaitUninterruptibly();
		if (future.isSuccess()) {
			return future.getNow();
		} else {
			if (future.cause() instanceof RpcException) {
				throw (RpcException) future.cause();
			} else {
				throw new RpcException(future.cause());
			}
		}
	}

	/**
	 * 异步发送需要响应的请求
	 * 
	 * @return future, 当接受到响应时, 该future成功完成, 否则, 该future失败完成, 无论如何, 该future都确保在timeoutMillis时间内完成
	 */
	public <V> IFuture<V> requestAsync(Object data, int timeoutMillis) {
		final RpcRequest request = new RpcRequest(data, false);
		request.setSerializerCode(options.getSerializerCode());
		final ResponseFuture<V> future = new ResponseFuture<>(request.getId(), timeoutMillis);
		Channel ch = null;
		try {
			ch = channelGroup.getChannel(options.getConnectTimeoutMillis());
		} catch (TimeoutException e) {
			ResponseFuture.doneWithException(future.getFutureId(), e);
		}
		NettyUtils.writeAndFlush(ch, request).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				if (!channelFuture.isSuccess()) {
					ResponseFuture.doneWithException(future.getFutureId(), channelFuture.cause());
				}
			}
		});
		return future;
	}
	
	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}
		channelGroup.close();
		workerGroup.shutdownGracefully();
	}
}

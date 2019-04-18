package messaging.client;

import java.io.IOException;
import java.io.Serializable;
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
import messaging.common.codec.Decoder;
import messaging.common.codec.Encoder;
import messaging.common.protocol.RpcRequest;
import messaging.util.Endpoint;
import messaging.util.NettyUtils;
import messaging.util.concurrent.DefaultPromise;
import messaging.util.concurrent.IFuture;
import messaging.util.concurrent.NamedThreadFactory;

/**
 * 
 * 
 * @author lixiaohui
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
		this.channelGroup = new ChannelGroup(options.getEndpoint(), createBootstrap(), options.getMaxConnections(),
				HealthChecker.ACTIVE);
	}

	private Bootstrap createBootstrap() {
		Bootstrap b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		Endpoint endpoint = options.getEndpoint();
		b.remoteAddress(endpoint.getIp(), endpoint.getPort());
		b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.getConnectTimeoutMillis());
		b.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				logger.info("Channel connected, channel = {}", ch);
				ch.closeFuture().addListener((future) -> {
					logger.info("Channel disconnected, channel = {}", ch);
				});

				ChannelPipeline pl = ch.pipeline();
				pl.addLast(new Decoder());
				pl.addLast(new Encoder());
				pl.addLast(new ResponseHandler());
			}
		});
		return b;
	}

	/**
	 * 发送无响应的请求
	 */
	public <T extends Serializable> IFuture<Void> send(T data) {
		final DefaultPromise<Void> promise = new DefaultPromise<>();
		try {
			Channel ch = channelGroup.getChannel(options.getConnectTimeoutMillis());
			final RpcRequest request = new RpcRequest(data, true);
			request.setSerializerCode(options.getSerializerCode());
			NettyUtils.writeAndFlush(ch, request).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (channelFuture.isSuccess()) {
						promise.setSuccess(true);
					} else {
						promise.setFailure(channelFuture.cause());
					}
				}
			});
		} catch (TimeoutException e) {
			promise.setFailure(e);
		}
		return promise;
	}

	/**
	 * 发送需要响应的请求
	 */
	public <V, T extends Serializable> IFuture<V> request(T data) {
		final RpcRequest request = new RpcRequest(data, false);
		request.setSerializerCode(options.getSerializerCode());
		final ResponseFuture<V> future = new ResponseFuture<>(request.getId(), options.getRequestTimeoutMillis());
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

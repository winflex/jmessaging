package messaging.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import io.netty.channel.Channel;
import messaging.common.RpcResult;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcResponse;
import messaging.util.NettyUtils;

/**
 * 上下文
 * 1. 封装请求源头的信息
 * 2. 回写响应
 * 
 * @author winflex
 */
public class Context {
	
	private final Channel channel;
	private final Executor executor;
	private final RpcMessage request;
	
	Context(RpcMessage request, Channel channel, Executor executor) {
		this.request = request;
		this.channel = channel;
		this.executor = executor;
	}

	public final String getRemoteHost() {
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		return remote.getAddress().getHostAddress();
	}
	
	public final int getRemotePort() {
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		return remote.getPort();
	}
	
	public final Executor getExecutor() {
		return this.executor;
	}
	
	public void writeResponse(Object data) {
		final RpcMessage message = new RpcResponse(request.getId(), RpcResult.newSuccessResult(data));
		message.setSerializerCode(request.getSerializerCode());
		NettyUtils.writeAndFlush(channel, message);
	}
}

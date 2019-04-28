package messaging.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import messaging.common.RpcResult;
import messaging.common.protocol.HeartbeatMessage;
import messaging.common.protocol.RpcResponse;
import messaging.util.NettyUtils;

/**
 * 
 * @author winflex
 */
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

	private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

	// heartbeat will always be the same
	private static final HeartbeatMessage HEARTBEAT = new HeartbeatMessage();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse resp) throws Exception {
		RpcResult result = (RpcResult) resp.getData();
		if (result.isSuccess()) {
			ResponseFuture.doneWithResult(resp.getId(), result.getResult());
		} else {
			ResponseFuture.doneWithException(resp.getId(), result.getCause());
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			logger.debug("Idle event({}) triggered on channel({})", ((IdleStateEvent) evt).state(), ctx.channel());
			NettyUtils.writeAndFlush(ctx.channel(), HEARTBEAT);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
	}
}

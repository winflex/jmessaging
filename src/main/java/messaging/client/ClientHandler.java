package messaging.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import messaging.common.RpcResult;
import messaging.common.protocol.HeartbeatMessage;
import messaging.common.protocol.RpcResponse;
import messaging.util.NettyUtils;

/**
 * 
 * @author winflex
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

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
			log.debug("Idle event({}) triggered on channel({})", ((IdleStateEvent) evt).state(), ctx.channel());
			NettyUtils.writeAndFlush(ctx.channel(), HEARTBEAT);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause.getMessage(), cause);
	}
}
